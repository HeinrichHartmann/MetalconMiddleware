package de.metalcon.middleware.controller.entity.impl.band;

import org.springframework.stereotype.Component;

import de.metalcon.middleware.controller.entity.generator.TracksTabGenerator;
import de.metalcon.middleware.domain.entity.Entity;
import de.metalcon.middleware.view.entity.tab.content.EntityTabContent;
import de.metalcon.middleware.view.entity.tab.preview.EntityTabPreview;

@Component
public class BandTracksTabGenerator extends TracksTabGenerator {

    @Override
    public void generateTabContent(EntityTabContent tabContent, Entity entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void genereteTabPreview(EntityTabPreview tabPreview, Entity entity) {
        // TODO Auto-generated method stub
    }

}
