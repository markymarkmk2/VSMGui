/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.vaadin.GuiElems.Dialogs;

import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.dimm.vsm.net.AttributeContainer;
import de.dimm.vsm.net.VSMAclEntry;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.vaadin.VSMCMain;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;






class MultipleSelect<T> extends VerticalLayout 
{
    OptionGroup optionGroup;
    List<T> options;

    public MultipleSelect(String caption, List<T> otpions, Property.ValueChangeListener listener) {
        this.options = otpions;        
        optionGroup = new OptionGroup(caption, options);
        optionGroup.setMultiSelect(true);
        optionGroup.setNullSelectionAllowed(false); // user can not 'unselect'
        optionGroup.setImmediate(true); // send the change to the server at once
        if (listener != null)
            optionGroup.addListener(listener); // react when the user selects something
        addComponent(optionGroup);
    }
    public void setValue( Set<T> option )
    {
        optionGroup.setValue(option);        
    }
    public Set<T> getSelectedValues()
    {
        return (Set<T>)optionGroup.getValue();        
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T t : getSelectedValues())
        {
            sb.append(t.toString());
        }
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    
}


class VSMAclEntryEditor extends VerticalLayout
{
     TextField principal;
     ComboBox type;
     CheckBox group;

    
     MultipleSelect<AclEntryFlag> flags;
     MultipleSelect<AclEntryPermission> permissions;
     TextField flagsText;
     TextField permissionsText;
     VSMCMain main;
     boolean selected = false;

    public VSMAclEntryEditor( VSMCMain main, LayoutClickListener listener) {
        this.main = main;
        buildGui();
        addListener(listener);
    }

    public boolean isSelected() {
        return selected;
    }
      
    
     
     void setAclSected( boolean v )
     {
         if (selected && v)
             selected = false; // Toggle
         else 
             selected = v;
         setStyleName(selected ? "acl-selected": "acl-non-selected");
     }
     final void buildGui() 
     {
         this.setImmediate(true);
         
         principal = new TextField("Principal");
         flagsText = new TextField("Flags");
         //flagsText.setReadOnly(true);
         permissionsText = new TextField("Permissions");
         //permissionsText.setReadOnly(true);
         
         
         addComponent(principal);
         addComponent(permissionsText);
         addComponent(flagsText);
         
         List<String> options = new ArrayList<>();
         options.add("ALLOW");
         options.add("DENY");
         options.add("AUDIT");
         options.add("ALARM");         
         type = new ComboBox("Typ", options);
         addComponent(type);
         
         group = new CheckBox("is group");
         addComponent(group);
         
         List<AclEntryPermission> permissionsData = Arrays.asList(AclEntryPermission.values());
         List<AclEntryFlag> flagsData = Arrays.asList(AclEntryFlag.values());
         
         permissions = new MultipleSelect("Permissions", permissionsData, null);                  
         flags = new MultipleSelect("Flags", flagsData, null);
         
         
     }

    void setAclEntry( VSMAclEntry entry ) {
        principal.setValue(entry.principalName());
        type.setValue(entry.type().toString());
        group.setValue(entry.isGroup());
        
        permissions.setValue( entry.permissions() );
        flags.setValue( entry.flags() );
        
        permissionsText.setValue( permissions.toString() );
        flagsText.setValue( flags.toString() );        
    }
    
    VSMAclEntry getAclEntry() {
        
        AclEntryType acltype  = AclEntryType.valueOf(type.getValue().toString());
        
        VSMAclEntry entry = new VSMAclEntry(acltype, principal.getValue().toString(), group.booleanValue(), 
                permissions.getSelectedValues(), flags.getSelectedValues() );        
        return entry;        
    }

    void handleClick( LayoutEvents.LayoutClickEvent event ) {
        if (event.getChildComponent() == permissionsText) {
            Window w = new Window("Permissions");
                    w.addComponent(permissions);
                    main.getRootWin().addWindow(w);
        }
        if (event.getChildComponent() == flagsText) {
                    Window w = new Window("Flags");
                    w.addComponent(flags);
                    main.getRootWin().addWindow(w);
        }
        setAclSected( event.getSource() == this);
    }

}
/**
 *
 * @author Administrator
 */
public class AttributeContainerEditor extends Window implements LayoutClickListener{
    
    VerticalLayout vl = new VerticalLayout();
    
    
    TextField txt_uname;
    HashMap<VSMAclEntry,VSMAclEntryEditor> editorMap = new HashMap<>();
    VerticalLayout aclLayout;
    VSMCMain main;

    public AttributeContainerEditor(VSMCMain main, String name, AttributeContainer ac) {
        this.main = main;
        build_gui(name, ac);
    }
    
    
    final void build_gui( String name, AttributeContainer ac )
    {
        removeAllComponents();
        addComponent(vl);
        
        setModal(true);
        setStyleName("vsm");

        //this.setSizeFull();
        this.setWidth("400px");

        vl.setSpacing(true);
        vl.setSizeFull();
        vl.setImmediate(true);
        vl.setStyleName("editWin"); 
        
        this.setCaption(VSMCMain.Txt("ACL-Editor"));
        TextField txtName = new TextField(VSMCMain.Txt("Name"), name );
        vl.addComponent(txtName);
        txtName.setWidth("100%");
        vl.setExpandRatio(txtName, 1.0f);
        String path = "";
        
        TextField txt_path = new TextField(VSMCMain.Txt("Pfad"), path );
        vl.addComponent(txt_path);
        txt_path.setWidth("100%");
        vl.setExpandRatio(txt_path, 1.0f);
        
 
        txt_uname = new TextField(VSMCMain.Txt("Username"), ac.getUserName() );
        vl.addComponent(txt_uname);
        txt_uname.setWidth("100%");
        vl.setExpandRatio(txt_uname, 1.0f);
        
        aclLayout = new VerticalLayout();
        vl.addComponent(aclLayout);
        
        for (VSMAclEntry entry: ac.getAcl()) {
            VSMAclEntryEditor ed = createAclEntryEditor( entry);
            aclLayout.addComponent(ed);
            editorMap.put(entry, ed);            
        }  
        HorizontalLayout btLayout = new HorizontalLayout();
        vl.addComponent(btLayout);
        Button bt = new Button("+", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {
                VSMAclEntry entry = new VSMAclEntry(AclEntryType.ALLOW, "", false, new HashSet<AclEntryPermission>(), new HashSet<AclEntryFlag>());
                VSMAclEntryEditor ed = createAclEntryEditor( entry);
                aclLayout.addComponent(ed);
                editorMap.put(entry, ed);            
            }
        });
        btLayout.addComponent(bt);
        bt = new Button("-", new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {

                for (Entry<VSMAclEntry,VSMAclEntryEditor> edEntry: editorMap.entrySet()) {
                    if (edEntry.getValue().isSelected()) {
                        editorMap.remove(edEntry.getKey());
                        aclLayout.removeComponent(edEntry.getValue());
                        break;
                    }
                }                     
            }
        });
        btLayout.addComponent(bt);
    }
    
   
    VSMAclEntryEditor createAclEntryEditor(VSMAclEntry entry) {
        VSMAclEntryEditor ed = new VSMAclEntryEditor(main, this);
        ed.setAclEntry( entry );
        return ed;
    }

    @Override
    public void layoutClick( LayoutEvents.LayoutClickEvent event ) {
        
        for (VSMAclEntryEditor ed: editorMap.values())
        {
            ed.handleClick( event );
        }
    }
    
}
