package com.dcos.utilities.utils;

import com.dcos.utilities.springconfiguration.properties.Marathon;
import com.dcos.utilities.springconfiguration.properties.Service;
import com.dcos.utilities.springconfiguration.properties.F5Exporter;
import com.dcos.utilities.springconfiguration.properties.Props;

public class PropsUtil {

    public static Props createDummyPorperties() {
        return Props.PropsBuilder.builder()
                .withF5Exporter(F5Exporter.F5ExporterBuilder.builder().withUrl("dummy").build())
                .withMarathon(Marathon.MarathonBuilder.builder().withUrl("dummy").build())
                .withService(Service.ServiceBuilder.builder().withVersion("1.0").withSchedulingIntervalInSecs(10).build())
                .build();
    }

}
