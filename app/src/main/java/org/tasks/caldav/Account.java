package org.tasks.caldav;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

public class Account {

    public static final String AUTHORITY = "org.tasks";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_URL = "url";

    private AccountManager accountManager;
    private android.accounts.Account account;

    public Account(AccountManager accountManager, android.accounts.Account account) {
        this.accountManager = accountManager;
        this.account = account;
    }

    public String getName() {
        return account.name;
    }

    public String getUrl() {
        return accountManager.getUserData(account, EXTRA_URL);
    }

    public String getUsername() {
        return accountManager.getUserData(account, EXTRA_USERNAME);
    }

    public String getPassword() {
        return accountManager.getPassword(account);
    }

    public android.accounts.Account getAccount() {
        return account;
    }

    public void setPassword(String password) {
        accountManager.setPassword(account, password);
    }

    public void setUsername(String username) {
        accountManager.setUserData(account, EXTRA_USERNAME, username);
    }

    public void setUrl(String url) {
        accountManager.setUserData(account, EXTRA_URL, url);
    }

    public boolean isBackgroundSyncEnabled() {
        return ContentResolver.getSyncAutomatically(account, AUTHORITY);
    }

    public void setSynchronizationEnabled(boolean enabled) {
        ContentResolver.setSyncAutomatically(account, AUTHORITY, enabled);
        if (enabled) {
            ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, TimeUnit.HOURS.toSeconds(1));
        } else {
            ContentResolver.removePeriodicSync(account, AUTHORITY, Bundle.EMPTY);
        }
    }
}
