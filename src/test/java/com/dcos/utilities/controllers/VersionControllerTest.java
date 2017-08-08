package com.dcos.utilities.controllers;

import com.dcos.utilities.springconfiguration.properties.Props;
import com.dcos.utilities.utils.PropsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VersionControllerTest {


    @Mock
    private BindingResult bindingResult;

    @Test
    public void shouldReturnVersionFromPropertiies() {
        Props props = PropsUtil.createDummyPorperties();
        VersionController versionController = new VersionController(props);
        when(bindingResult.hasErrors()).thenReturn(false);

        assertThat(versionController.version().getStatusCode(), is(HttpStatus.OK));
        assertThat(versionController.version().getBody(), is("1.0"));


    }

}
