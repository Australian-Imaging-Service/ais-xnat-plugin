/*
 * xnat-template: org.nrg.xnat.plugins.template.rest.TemplatePrefsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.plugins.template.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.plugins.template.preferences.TemplatePreferencesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;

@Api
@XapiRestController
@RequestMapping(value = "/template/prefs")
@Slf4j
public class TemplatePrefsApi extends AbstractXapiRestController {
    @Autowired
    public TemplatePrefsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final TemplatePreferencesBean templatePrefs) {
        super(userManagementService, roleHolder);
        _templatePrefs = templatePrefs;
    }

    @ApiOperation(value = "Returns the full map of template preferences.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authenticated)
    public Map<String, Object> getTemplatePreferences() {
        return _templatePrefs;
    }

    @ApiOperation(value = "Returns the value of the specified template preference.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Template preference value successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access template preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{preference}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authenticated)
    public String getPreferenceValue(@ApiParam(value = "The template preference to retrieve.", required = true) @PathVariable final String preference) throws NotFoundException {
        if (!_templatePrefs.containsKey(preference)) {
            throw new NotFoundException("No preference named \"" + preference + "\" was found.");
        }
        return _templatePrefs.getValue(preference);
    }

    @ApiOperation(value = "Updates the value of the specified template preference.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Template preference value successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access template preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{preference}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Authenticated)
    public String setPreferenceValue(@ApiParam(value = "The template preference to set.", required = true) @PathVariable final String preference,
                                     @ApiParam(value = "The template preference to set.", required = true) @RequestBody final String value) throws NotFoundException, NrgServiceException {
        if (!_templatePrefs.containsKey(preference)) {
            throw new NotFoundException("No preference named \"" + preference + "\" was found.");
        }
        try {
            return _templatePrefs.set(value, preference);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            throw new NrgServiceException(ConfigurationError, "An error occurred trying to set the \"" + preference + "\" template preference to the value: " + value);
        }
    }

    private final TemplatePreferencesBean _templatePrefs;
}
