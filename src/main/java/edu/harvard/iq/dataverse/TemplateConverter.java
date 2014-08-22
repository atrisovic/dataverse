/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dataverse;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author skraffmiller
 */
@FacesConverter("templateConverter")
public class TemplateConverter implements Converter {
    
    @EJB
    TemplateServiceBean templateService;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        System.out.print("get as Object " + submittedValue.toString());
        Template template = templateService.find(new Long(submittedValue));
        System.out.print("find Object " + template.getName());
        //return new Long(submittedValue);
        //return (Template) templateService.find(new Long(submittedValue));
        return  template;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            System.out.print("get as string " + value.toString());
            return ((Template) value).getId().toString();
        }
    }
    
}
