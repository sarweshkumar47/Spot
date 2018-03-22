package com.makesense.labs.spot.main;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 * @description Interface provides a method which confirms user's logout action
 */
public interface AppLogOutListener {
    void onAppSignOutConfirm(boolean accountDelete);
}
