/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.org                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package org.datacrow.client.console.wizards.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.wizards.IWizardPanel;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcChildModule;
import org.datacrow.core.modules.DcMediaChildModule;
import org.datacrow.core.modules.DcMediaModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.ModuleJar;
import org.datacrow.core.modules.xml.XmlField;
import org.datacrow.core.modules.xml.XmlModule;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class RelateModuleWizard extends Wizard {

    public RelateModuleWizard() {
        super();
        setTitle(DcResources.getText("lblModuleRelateWizard"));
        setHelpIndex("dc.modules.relate");

        setSize(DcSettings.getDimension(DcRepository.Settings.stModuleWizardFormSize));
        setCenteredLocation();
    }
    
    @Override
    protected void initialize() {}
    
    @Override
    protected List<IWizardPanel> getWizardPanels() {
        List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
        panels.add(new PanelSelectParentModule(this));
        panels.add(new PanelSelectChildModule(this));
        return panels;
    }

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stModuleWizardFormSize, getSize());
    }

    @Override
    public void finish() throws WizardException {
        XmlModule parentModule = (XmlModule) getCurrent().apply();

        try {
            ModuleJar mjParent = new ModuleJar(parentModule);
            mjParent.save();
            
            XmlModule childModule = DcModules.get(parentModule.getChildIndex()).getXmlModule();
            childModule.setParentIndex(parentModule.getIndex());
            
            if (childModule.getModuleClass().equals(DcMediaModule.class))
                childModule.setModuleClass(DcMediaChildModule.class);
            else
                childModule.setModuleClass(DcChildModule.class);
            
            Collection<XmlField> fields = childModule.getFields();
            XmlField field = new XmlField();
            field.setColumn(parentModule.getTableName() + "_ID");
            field.setName(parentModule.getObjectName());
            field.setFieldType(ComponentFactory._SHORTTEXTFIELD);
            field.setMaximumLength(50);
            field.setModuleReference(parentModule.getIndex());
            field.setReadonly(true);
            field.setEnabled(false);
            field.setSearchable(false);
            field.setUiOnly(false);
            field.setOverwritable(false);
            field.setValueType(DcRepository.ValueTypes._DCPARENTREFERENCE);
            fields.add(field);
            childModule.setFields(fields);
            
            ModuleJar mjChild = new ModuleJar(childModule);
            mjChild.save();
            
            close();
        } catch (Exception e) {
            throw new WizardException(DcResources.getText("msgCouldNotWriteModuleFile", e.getMessage()));
        }
    }

    @Override
    public void next() {
        try {
            XmlModule module = (XmlModule) getCurrent().apply();

            if (module == null)
                return;
            
            current += 1;
            if (current <= getStepCount()) {
                ModuleWizardPanel panel;
                for (int i = 0; i < getStepCount(); i++) {
                    panel = (ModuleWizardPanel) getWizardPanel(i);
                    panel.setModule(module);
                    panel.setVisible(i == current);
                }
            } else {
                current -= 1;
            }

            applyPanel();
        } catch (WizardException wzexp) {
            if (wzexp.getMessage().length() > 1)
                GUI.getInstance().displayWarningMessage(wzexp.getMessage());
        }
    }

    @Override
    public void close() {
        if (!isCancelled() && !isRestarted())
            new RestartDataCrowDialog(this);
        
        super.close();
    }

    @Override
    protected String getWizardName() {
        return DcResources.getText("msgModuleWizard",
                                   new String[] {String.valueOf(current + 1), String.valueOf(getStepCount())});
    }

    @Override
    protected void restart() {
        try {
            finish();
            saveSettings();
            CreateModuleWizard wizard = new CreateModuleWizard();
            wizard.setVisible(true);
        } catch (WizardException exp) {
            GUI.getInstance().displayWarningMessage(exp.getMessage());
        }
    }
}
