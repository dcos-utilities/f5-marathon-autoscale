package com.dcos.utilities.controllers;

import com.dcos.utilities.springconfiguration.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = "/version")
public class VersionController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Props props;

    @Inject
    public VersionController(Props props){
        this.props =  props;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> version() {
        return ResponseEntity.ok().body(props.getService().getVersion());
    }
}
