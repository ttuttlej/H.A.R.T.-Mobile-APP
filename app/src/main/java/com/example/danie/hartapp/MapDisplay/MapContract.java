package com.example.danie.hartapp.MapDisplay;

/**
 * Defines the contract between the View {@link MapActivity} and the presenter
 * {@link MapPresenter}.
 *
 * <P>This interface is designed to create a very controlled link as to how the
 * Classes within the map module will function.
 *
 * @see MapPresenter
 * @see MapActivity
 */

public interface MapContract {

        interface MapView{
            void initMap();
        }

        interface MPresenter {
        }
}