package de.vote.web;

import de.vote.LocaleBean;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

/**
 * Simple {@link Date} converter for date input fields which uses the date 
 * format provided by the {@link LocaleBean} according to the current locale 
 * of the user.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@FacesConverter("VoteDateConverter")
public class DateConverter implements Converter {

    @Inject
    private LocaleBean localeBean;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Date date = null;
        try {
            date = localeBean.getDateFormatForCurrentLocale().parse(value);
        } catch (Exception ex) {
            throw new ConverterException(
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            localeBean.getMessage("EditPoll.error.dateFormat"),
                            null
                    )
            );
        }
        
        return date;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {        
        if (value == null) {
            return "";
        }
        
        return localeBean.getDateFormatForCurrentLocale().format((Date) value);
    }
    
}
