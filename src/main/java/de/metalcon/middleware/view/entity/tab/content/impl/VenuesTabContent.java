package de.metalcon.middleware.view.entity.tab.content.impl;

import de.metalcon.middleware.view.entity.tab.EntityTabType;
import de.metalcon.middleware.view.entity.tab.content.EntityTabContent;

public class VenuesTabContent extends EntityTabContent {

    @Override
    public EntityTabType getEntityTabType() {
        return EntityTabType.VENUES_TAB;
    }

}
