package com.example.danie.hartapp.Main;

import android.view.View;

/**
 * Defines the contract between the View {@link MainActivity}
 * and the presenter {@link MainPresenter}.
 *
 * <P>This interface is designed to create a very controlled link as to how the
 * Classes within the Main module will function. Contract should be updated when
 * a button is addes to the Main screen.</P>
 *
 * @see MainPresenter
 * @see MainActivity
 */

public interface MainContract {

    interface MainView{
    }

    interface Presenter {
        void handleButtonClick(View view);
    }

}
