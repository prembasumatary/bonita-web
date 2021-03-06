/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.forms.server.validator;

import java.util.Locale;

import org.bonitasoft.forms.client.model.FormFieldValue;

/**
 * Validator forbidding anything else than a Double value in a form field
 * 
 * @author Anthony Birembaut
 * 
 */
public class NumericDoubleFieldValidator implements IFormFieldValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final FormFieldValue fieldInput, final Locale locale) {
        if (fieldInput.getValue() instanceof Double) {
            return true;
        } else if (fieldInput.getValue() instanceof String) {
            final String fieldValue = (String) fieldInput.getValue();
            try {
                if (fieldValue != null && fieldValue.length() > 0) {
                    Double.parseDouble(fieldValue);
                }
                return true;
            } catch (final NumberFormatException e) {
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Double decimal validator";
    }
}
