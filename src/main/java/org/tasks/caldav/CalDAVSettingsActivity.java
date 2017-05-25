/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package org.tasks.caldav;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.todoroo.andlib.sql.Criterion;
import com.todoroo.astrid.activity.TaskListActivity;
import com.todoroo.astrid.api.CaldavFilter;
import com.todoroo.astrid.dao.CaldavDao;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.data.CaldavAccount;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.helper.UUIDHelper;
import com.todoroo.astrid.tags.CaldavMetadata;

import org.tasks.R;
import org.tasks.activities.ColorPickerActivity;
import org.tasks.analytics.Tracker;
import org.tasks.analytics.Tracking;
import org.tasks.dialogs.ColorPickerDialog;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.injection.ActivityComponent;
import org.tasks.injection.ThemedInjectingAppCompatActivity;
import org.tasks.preferences.Preferences;
import org.tasks.sync.SyncAdapters;
import org.tasks.themes.ThemeCache;
import org.tasks.themes.ThemeColor;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.tasks.time.DateTimeUtils.currentTimeMillis;

public class CalDAVSettingsActivity extends ThemedInjectingAppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final String EXTRA_SELECTED_THEME = "extra_selected_theme";

    private static final int REQUEST_COLOR_PICKER = 10109;

    public static final String EXTRA_CALDAV_DATA = "caldavData"; //$NON-NLS-1$
    public static final String EXTRA_CALDAV_UUID = "uuid"; //$NON-NLS-1$

    public static final String ACTION_RELOAD = "accountRenamed";
    public static final String ACTION_DELETED = "accountDeleted";

    private boolean isNewAccount;
    private CaldavAccount caldavAccount;
    private Account localAccount;
    private int selectedTheme;

    @Inject MetadataDao metadataDao;
    @Inject DialogBuilder dialogBuilder;
    @Inject Preferences preferences;
    @Inject ThemeCache themeCache;
    @Inject ThemeColor themeColor;
    @Inject Tracker tracker;
    @Inject CaldavDao caldavDao;
    @Inject CaldavAccountManager caldavAccountManager;
    @Inject SyncAdapters syncAdapters;

    @BindView(R.id.root_layout) LinearLayout root;
    @BindView(R.id.name) TextInputEditText name;
    @BindView(R.id.url) TextInputEditText url;
    @BindView(R.id.user) TextInputEditText user;
    @BindView(R.id.password) TextInputEditText password;
    @BindView(R.id.name_layout) TextInputLayout nameLayout;
    @BindView(R.id.url_layout) TextInputLayout urlLayout;
    @BindView(R.id.user_layout) TextInputLayout userLayout;
    @BindView(R.id.password_layout) TextInputLayout passwordLayout;
    @BindView(R.id.color) TextInputEditText color;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.background_sync) CheckBox backgroundSync;
    @BindView(R.id.master_sync_warning) TextView masterSyncWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_caldav_settings);
        ButterKnife.bind(this);

        caldavAccount = getIntent().getParcelableExtra(EXTRA_CALDAV_DATA);
        if (caldavAccount == null) {
            isNewAccount = true;
            caldavAccount = new CaldavAccount();
            caldavAccount.setUUID(UUIDHelper.newUUID());
        } else {
            localAccount = caldavAccountManager.getAccount(caldavAccount.getName());
        }
        if (savedInstanceState == null) {
            selectedTheme = caldavAccount.getColor();
        } else {
            selectedTheme = savedInstanceState.getInt(EXTRA_SELECTED_THEME);
        }

        final boolean backButtonSavesTask = preferences.backButtonSavesTask();
        toolbar.setTitle(isNewAccount ? getString(R.string.add_account) : caldavAccount.getName());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this,
                backButtonSavesTask ? R.drawable.ic_close_24dp : R.drawable.ic_save_24dp));
        toolbar.setNavigationOnClickListener(v -> {
            if (backButtonSavesTask) {
                discard();
            } else {
                save();
            }
        });
        toolbar.inflateMenu(R.menu.menu_tag_settings);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.showOverflowMenu();

        color.setInputType(InputType.TYPE_NULL);

        name.setText(caldavAccount.getName());

        if (isNewAccount) {
            toolbar.getMenu().findItem(R.id.delete).setVisible(false);
            name.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
        } else if (localAccount != null) {
            url.setText(localAccount.getUrl());
            user.setText(localAccount.getUsername());
            password.setText(localAccount.getPassword());
        }

        backgroundSync.setChecked(localAccount == null || localAccount.isBackgroundSyncEnabled());

        updateTheme();
    }

    @OnTextChanged(R.id.name)
    void onNameChanged(CharSequence text) {
        nameLayout.setError(null);
    }

    @OnTextChanged(R.id.url)
    void onUrlChanged(CharSequence text) {
        urlLayout.setError(null);
    }

    @OnTextChanged(R.id.user)
    void onUserChanged(CharSequence text) {
        userLayout.setError(null);
    }

    @OnTextChanged(R.id.password)
    void onPasswordChanged(CharSequence text) {
        passwordLayout.setError(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_SELECTED_THEME, selectedTheme);
    }

    @Override
    protected void onResume() {
        super.onResume();

        masterSyncWarning.setVisibility(syncAdapters.isMasterSyncEnabled() ? View.GONE : View.VISIBLE);
    }

    @OnFocusChange(R.id.color)
    void onFocusChange(boolean focused) {
        if (focused) {
            color.clearFocus();
            showThemePicker();
        }
    }

    @OnClick(R.id.color)
    protected void showThemePicker() {
        Intent intent = new Intent(CalDAVSettingsActivity.this, ColorPickerActivity.class);
        intent.putExtra(ColorPickerActivity.EXTRA_PALETTE, ColorPickerDialog.ColorPalette.COLORS);
        intent.putExtra(ColorPickerActivity.EXTRA_SHOW_NONE, true);
        startActivityForResult(intent, REQUEST_COLOR_PICKER);
    }

    @Override
    public void inject(ActivityComponent component) {
        component.inject(this);
    }

    private String getNewName() {
        return name.getText().toString().trim();
    }

    private String getNewURL() {
        return url.getText().toString().trim();
    }

    private String getNewUsername() {
        return user.getText().toString().trim();
    }

    private String getNewPassword() {
        return password.getText().toString().trim();
    }

    private boolean clashes(String newName) {
        CaldavAccount existing = caldavDao.getAccountByName(newName);
        return existing != null && caldavAccount.getId() != existing.getId();
    }

    private void save() {
        String newName = getNewName();
        String username = getNewUsername();
        String url = getNewURL();
        String password = getNewPassword();

        boolean failed = false;

        if (isEmpty(newName)) {
            nameLayout.setError(getString(R.string.name_cannot_be_empty));
            failed = true;
        } else if (clashes(newName)) {
            nameLayout.setError(getString(R.string.tag_already_exists));
            failed = true;
        }

        if (isEmpty(url)) {
            urlLayout.setError(getString(R.string.url_required));
            failed = true;
        } else {
            Uri baseURL = Uri.parse(url);
            String scheme = baseURL.getScheme();
            if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
                String host = baseURL.getHost();
                if (isEmpty(host)) {
                    urlLayout.setError(getString(R.string.url_host_name_required));
                    failed = true;
                } else {
                    try {
                        host = IDN.toASCII(host);
                    } catch(Exception e) {
                        Timber.e(e.getMessage(), e);
                    }
                    String path = baseURL.getEncodedPath();
                    int port = baseURL.getPort();
                    try {
                        new URI(scheme, null, host, port, path, null, null);
                    } catch(URISyntaxException e) {
                        urlLayout.setError(e.getLocalizedMessage());
                        failed = true;
                    }
                }
            } else {
                urlLayout.setError(getString(R.string.url_invalid_scheme));
                failed = true;
            }
        }

        if (isEmpty(username)) {
            userLayout.setError(getString(R.string.username_required));
            failed = true;
        }

        if (isNewAccount && isEmpty(password)) {
            passwordLayout.setError(getString(R.string.password_required));
            failed = true;
        }

        if (failed) {
            return;
        }

        if (isNewAccount) {
            caldavAccount.setName(newName);
            caldavAccount.setColor(selectedTheme);
            if (caldavAccountManager.addAccount(caldavAccount, url, username, password)) {
                caldavDao.persist(caldavAccount);
                Account account = caldavAccountManager.getAccount(caldavAccount.getName());
                if (account == null) {
                    showErrorSnackbar();
                } else {
                    account.setSynchronizationEnabled(backgroundSync.isChecked());
                    setResult(RESULT_OK, new Intent().putExtra(TaskListActivity.OPEN_FILTER, new CaldavFilter(caldavAccount)));
                    finish();
                }
            } else {
                showErrorSnackbar();
            }
        } else if (hasChanges()) {
            if (!newName.equals(caldavAccount.getName())) {
                String oldName = caldavAccount.getName();
                caldavAccount.setName(newName);
                if (caldavAccountManager.addAccount(caldavAccount, url, username, password)) {
                    CaldavAccount oldAccount = new CaldavAccount();
                    oldAccount.setName(oldName);
                    oldAccount.setDeletionDate(currentTimeMillis());
                    caldavDao.createNew(oldAccount);
                    renameLinks(caldavAccount.getUUID(), newName);
                    localAccount = caldavAccountManager.getAccount(newName);
                } else {
                    nameLayout.setError(getString(R.string.error_renaming_account));
                    return;
                }
            }

            caldavAccount.setColor(selectedTheme);
            localAccount.setUrl(url);
            localAccount.setUsername(username);
            localAccount.setPassword(password);
            localAccount.setSynchronizationEnabled(backgroundSync.isChecked());
            caldavDao.persist(caldavAccount);
            setResult(RESULT_OK, new Intent(ACTION_RELOAD).putExtra(TaskListActivity.OPEN_FILTER, new CaldavFilter(caldavAccount)));
            finish();
        }
    }

    private void showErrorSnackbar() {
        Snackbar snackbar = Snackbar.make(root, getString(R.string.error_adding_account), 8000)
                .setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_text_color));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_background));
        snackbar.show();
    }

    private boolean hasChanges() {
        if (isNewAccount) {
            return selectedTheme >= 0 || !isEmpty(getNewName()) ||
                    !isEmpty(getNewPassword()) || !isEmpty(getNewURL()) ||
                    !isEmpty(getNewUsername()) || !backgroundSync.isChecked();
        }
        return selectedTheme != caldavAccount.getColor() ||
                !getNewName().equals(caldavAccount.getName()) ||
                !getNewURL().equals(localAccount.getUrl()) ||
                !getNewUsername().equals(localAccount.getUsername()) ||
                !getNewPassword().equals(localAccount.getPassword()) ||
                backgroundSync.isChecked() != localAccount.isBackgroundSyncEnabled();
    }

    @Override
    public void finish() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (preferences.backButtonSavesTask()) {
            save();
        } else {
            discard();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COLOR_PICKER) {
            if (resultCode == RESULT_OK) {
                int index = data.getIntExtra(ColorPickerActivity.EXTRA_THEME_INDEX, 0);
                tracker.reportEvent(Tracking.Events.SET_TAG_COLOR, Integer.toString(index));
                selectedTheme = index;
                updateTheme();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void deleteAccount() {
        dialogBuilder.newMessageDialog(R.string.delete_tag_confirmation, caldavAccount.getName())
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (caldavAccount != null) {
                        caldavAccount.setDeletionDate(currentTimeMillis());
                        caldavDao.persist(caldavAccount);
                        setResult(RESULT_OK, new Intent(ACTION_DELETED).putExtra(EXTRA_CALDAV_UUID, caldavAccount.getUuid()));
                    }
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void discard() {
        if (!hasChanges()) {
            finish();
        } else {
            dialogBuilder.newMessageDialog(R.string.discard_changes)
                    .setPositiveButton(R.string.discard, (dialog, which) -> finish())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    private void updateTheme() {
        ThemeColor themeColor;
        if (selectedTheme < 0) {
            themeColor = this.themeColor;
            color.setText(R.string.none);
        } else {
            themeColor = themeCache.getThemeColor(selectedTheme);
            color.setText(themeColor.getName());
        }
        themeColor.apply(toolbar);
        themeColor.applyToStatusBar(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteAccount();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renameLinks(String uuid, String newName) {
        Metadata metadataTemplate = new Metadata();
        metadataTemplate.setValue(CaldavMetadata.CALDAV_NAME, newName);

        metadataDao.update(
                Criterion.and(
                        MetadataDao.MetadataCriteria.withKey(CaldavMetadata.KEY),
                        CaldavMetadata.CALDAV_UUID.eq(uuid)),
                metadataTemplate);
    }
}
