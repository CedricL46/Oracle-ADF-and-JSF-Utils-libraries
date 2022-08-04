
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.binding.DCParameter;
import oracle.adf.share.logging.ADFLogger;

import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;
import oracle.binding.ControlBinding;
import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ValidationException;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.uicli.binding.JUCtrlValueBinding;


/**
 * A series of useful Oracle ADF functions
 *
 * @author Duncan Mills
 * @author Steve Muench
 * @author Cedric Leruth
 *
 */
public class ADFUtils {

    public static final ADFLogger LOGGER = ADFLogger.createADFLogger(ADFUtils.class);    
    
    
     /**
     * Function to get the ViewOject implementation for the iterator name
     * The iterator name of a specific binding can be found in his PageDef.xml 
     * (middle column named "Executables")
     * 
     * How to use examples : 
     * ViewObject vo = ADFUtils.getViewObjectFromIterator("YOUR_ITERATOR_NAME");
     * vo.getCurrentRow() (Get the selected row values)
     * vo.reset();  vo.clearCache();
     * vo.executeQuery(); (execute the SELECT)
     * boolean isNotSaved = vo.getApplicationModule().getTransaction().isDirty(); (TRUE if need to be commited or rollback)
     * vo.getApplicationModule().getTransaction().validate(); (Validate if all fields are ok)
     * vo.getApplicationModule().getTransaction().commit(); (Commit to DB)
     * vo.getApplicationModule().getTransaction().rollback(); (Rollback to DB)
     *
     * @author Cedric Leruth cedricleruth.com
     * @param iteratorName (The iterator name of a specific binding can be found in his PageDef.xml)
     * @return ViewObjectImpl or null if the param iteratorName doesn't exist
     */
    public static ViewObjectImpl getViewObjectFromIterator(String iteratorName) {
        ViewObjectImpl iteratorVO = null;
        try {
            DCBindingContainer currentBindingEntry = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
            DCIteratorBinding iteratorBinding = currentBindingEntry.findIteratorBinding(iteratorName);
            iteratorVO = (ViewObjectImpl) iteratorBinding.getViewObject();
        } catch (Exception exception) {
            //Occurs if currentBindingEntry or iteratorBinding is null
            //Usually if the iterator named iteratorName doesn't exist
            LOGGER.severe(exception.getMessage());
        }
        return iteratorVO;
    }
    
    /**
     * Execute a validate and commit on the viewObject of a binding iterator
     * The iterator name of a specific binding can be found in his PageDef.xml 
     * (middle column named "Executables")
     *
     * @author Cedric Leruth cedricleruth.com
     * @param iteratorName The iterator name of a specific binding can be found in his PageDef.xml
     * @return true if the validate and commit is succesful, false otherwise
     */
      public static boolean commitIterator(String iteratorName) {
        boolean commitSuccessful = false;
        ViewObject iteratorVO = getViewObjectFromIterator(iteratorName);
        try {
            iteratorVO.getApplicationModule().getTransaction().validate();
            iteratorVO.getApplicationModule().getTransaction().commit();
            commitSuccessful = true;
        } catch (ValidationException validationException) {            
            //Occurs if the data needing commit is invalid
            //example: if a user enter a String in a NUMBER DB column
            //The data isn't committed and you need to warn the user for him to fix this and retry
            LOGGER.warning(validationException.getMessage());
            //Warn the user
        } catch (Exception exception) {
            //Occurs if currentBindingEntry or iteratorBinding is null
            //Usually if the iterator named iteratorName doesn't exist
            LOGGER.severe(exception.getMessage());
        }
        return commitSuccessful;
    }
    
    
    /**
     * Execute a rollback on the viewObject of a binding iterator
     * The iterator name of a specific binding can be found in his PageDef.xml 
     * (middle column named "Executables")
     *
     * @author Cedric Leruth cedricleruth.com
     * @param iteratorName The iterator name of a specific binding can be found in his PageDef.xml
     * @return true if the rollback is succesful, false otherwise
     */
      public static boolean rollbackIterator(String iteratorName) {
        boolean rollbackSuccessful = false;
        ViewObject iteratorVO = getViewObjectFromIterator(iteratorName);
        try {
            iteratorVO.getApplicationModule().getTransaction().rollback();
            rollbackSuccessful = true;
        } catch (ValidationException validationException) {            
            //Occurs if the data needing commit is invalid
            //example: if a user enter a String in a NUMBER DB column
            //The data isn't committed and you need to warn the user for him to fix this and retry
            LOGGER.warning(validationException.getMessage());
            //Warn the user
        } catch (Exception exception) {
            //Occurs if currentBindingEntry or iteratorBinding is null
            //Usually if the iterator named iteratorName doesn't exist
            LOGGER.severe(exception.getMessage());
        }
        return rollbackSuccessful;
    }
    
     /**
     * How many lines return a specific Iterator without have to execute a SELECT COUNT
     *
     * How to use examples :
     * long val = ADFUtils.getIteratorEstimatedRowCount("YOUR_ITERATOR_NAME")
     *
     * @author Cedric Leruth cedricleruth.com
     * @param iteratorName The iterator name of a specific binding can be found in his PageDef.xml
     * @return long 0 or How many lines return a specific Iterator without have to execute a SELECT COUNT
     */
    public static long getIteratorEstimatedRowCount(String iteratorName) {
        long iteratorRowCount = 0;
        try {
            DCBindingContainer currentBindingEntry = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
            DCIteratorBinding iteratorBinding = currentBindingEntry.findIteratorBinding(iteratorName);
            iteratorRowCount = iteratorBinding.getEstimatedRowCount();
        } catch (Exception exception) {
            //Occurs if currentBindingEntry or iteratorBinding is null
            //Usually if the iterator named iteratorName doesn't exist
            LOGGER.severe(exception.getMessage());
        }
        return iteratorRowCount;
    }

    /**
     * Get application module for an application module data control by name.
     * @author Duncan Mills
     * @author Steve Muench
     * @param name application module data control name
     * @return ApplicationModule
     */
    public static ApplicationModule getApplicationModuleForDataControl(String name) {
        return (ApplicationModule) JSFUtils.resolveExpression("#{data." + name + ".dataProvider}");
    }

    /**
     * A convenience method for getting the value of a bound attribute in the
     * current page context programatically.
     * @author Duncan Mills
     * @author Steve Muench
     * @param attributeName of the bound value in the pageDef
     * @return value of the attribute
     */
    public static Object getBoundAttributeValue(String attributeName) {
        return findControlBinding(attributeName).getInputValue();
    }

    /**
     * A convenience method for setting the value of a bound attribute in the
     * context of the current page.
     * @author Duncan Mills
     * @author Steve Muench
     * @param attributeName of the bound value in the pageDef
     * @param value to set
     */
    public static void setBoundAttributeValue(String attributeName, Object value) {
        findControlBinding(attributeName).setInputValue(value);
    }

    /**
     * Returns the evaluated value of a pageDef parameter.
     * @author Duncan Mills
     * @author Steve Muench
     * @param pageDefName reference to the page definition file of the page with the parameter
     * @param parameterName name of the pagedef parameter
     * @return evaluated value of the parameter as a String
     */
    public static Object getPageDefParameterValue(String pageDefName, String parameterName) {
        BindingContainer bindings = findBindingContainer(pageDefName);
        DCParameter param = ((DCBindingContainer) bindings).findParameter(parameterName);
        return param.getValue();
    }

    /**
     * Convenience method to find a DCControlBinding as an AttributeBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @author Duncan Mills
     * @author Steve Muench
     * @param bindingContainer binding container
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(BindingContainer bindingContainer, String attributeName) {
        if (attributeName != null) {
            if (bindingContainer != null) {
                ControlBinding ctrlBinding = bindingContainer.getControlBinding(attributeName);
                if (ctrlBinding instanceof AttributeBinding) {
                    return (AttributeBinding) ctrlBinding;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to find a DCControlBinding as a JUCtrlValueBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @author Duncan Mills
     * @author Steve Muench
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(String attributeName) {
        return findControlBinding(getBindingContainer(), attributeName);
    }

    /**
     * Return the current page's binding container.
     * @return the current page's binding container
     * @author Duncan Mills
     * @author Steve Muench
     */
    public static BindingContainer getBindingContainer() {
        return (BindingContainer) JSFUtils.resolveExpression("#{bindings}");
    }

    /**
     * Return the Binding Container as a DCBindingContainer.
     * @return current binding container as a DCBindingContainer
     * @author Duncan Mills
     * @author Steve Muench
     */
    public static DCBindingContainer getDCBindingContainer() {
        return (DCBindingContainer) getBindingContainer();
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName, String valueAttrName, String displayAttrName) {
        return selectItemsForIterator(findIterator(iteratorName), valueAttrName, displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute to use for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName, String valueAttrName, String displayAttrName, String descriptionAttrName) {
        return selectItemsForIterator(findIterator(iteratorName), valueAttrName, displayAttrName, descriptionAttrName);
    }

    /**
     * Get List of attribute values for an iterator.
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName value attribute to use
     * @return List of attribute values for an iterator
     */
    public static List attributeListForIterator(String iteratorName, String valueAttrName) {
        return attributeListForIterator(findIterator(iteratorName), valueAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName iterabot binding name
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(String iteratorName) {
        return keyListForIterator(findIterator(iteratorName));
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter iterator binding
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(DCIteratorBinding iter) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getKey());
        }
        return attributeList;
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName iterator binding name
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(String iteratorName, String keyAttrName) {
        return keyAttrListForIterator(findIterator(iteratorName), keyAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter iterator binding
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(DCIteratorBinding iter, String keyAttrName) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(new Key(new Object[] { r.getAttribute(keyAttrName) }));
        }
        return attributeList;
    }

    /**
     * Get a List of attribute values for an iterator.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter iterator binding
     * @param valueAttrName name of value attribute to use
     * @return List of attribute values
     */
    public static List attributeListForIterator(DCIteratorBinding iter, String valueAttrName) {
        List attributeList = new ArrayList();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getAttribute(valueAttrName));
        }
        return attributeList;
    }

    /**
     * Find an iterator binding in the current binding container by name.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param name iterator binding name
     * @return iterator binding
     */
    public static DCIteratorBinding findIterator(String name) {
        DCIteratorBinding iter = getDCBindingContainer().findIteratorBinding(name);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + name + "' not found");
        }
        return iter;
    }

    /**
     * @author Duncan Mills
     * @author Steve Muench
     * @param bindingContainer
     * @param iterator
     * @return
     */
    public static DCIteratorBinding findIterator(String bindingContainer, String iterator) {
        DCBindingContainer bindings = (DCBindingContainer) JSFUtils.resolveExpression("#{" + bindingContainer + "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" + bindingContainer + "' not found");
        }
        DCIteratorBinding iter = bindings.findIteratorBinding(iterator);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + iterator + "' not found");
        }
        return iter;
    }

    /**
     * @author Duncan Mills
     * @author Steve Muench
     * @param name
     * @return
     */
    public static JUCtrlValueBinding findCtrlBinding(String name) {
        JUCtrlValueBinding rowBinding = (JUCtrlValueBinding) getDCBindingContainer().findCtrlBinding(name);
        if (rowBinding == null) {
            throw new RuntimeException("CtrlBinding " + name + "' not found");
        }
        return rowBinding;
    }

    /**
     * Find an operation binding in the current binding container by name.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param name operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String name) {
        OperationBinding op = getDCBindingContainer().getOperationBinding(name);
        if (op == null) {
            throw new RuntimeException("Operation '" + name + "' not found");
        }
        return op;
    }

    /**
     * Find an operation binding in the current binding container by name.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param bindingContianer binding container name
     * @param opName operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String bindingContianer, String opName) {
        DCBindingContainer bindings = (DCBindingContainer) JSFUtils.resolveExpression("#{" + bindingContianer + "}");
        if (bindings == null) {
            throw new RuntimeException("Binding container '" + bindingContianer + "' not found");
        }
        OperationBinding op = bindings.getOperationBinding(opName);
        if (op == null) {
            throw new RuntimeException("Operation '" + opName + "' not found");
        }
        return op;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter, String valueAttrName, String displayAttrName, String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName), (String) r.getAttribute(displayAttrName), (String) r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter, String valueAttrName, String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName), (String) r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName, String displayAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName), displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName, String displayAttrName, String descriptionAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName), displayAttrName, descriptionAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter, String displayAttrName, String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(), (String) r.getAttribute(displayAttrName), (String) r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return List of ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter, String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(), (String) r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Find the BindingContainer for a page definition by name.
     *
     * Typically used to refer eagerly to page definition parameters. It is
     * not best practice to reference or set bindings in binding containers
     * that are not the one for the current page.
     *
     * @author Duncan Mills
     * @author Steve Muench
     * @param pageDefName name of the page defintion XML file to use
     * @return BindingContainer ref for the named definition
     */
    private static BindingContainer findBindingContainer(String pageDefName) {
        BindingContext bctx = getDCBindingContainer().getBindingContext();
        BindingContainer foundContainer = bctx.findBindingContainer(pageDefName);
        return foundContainer;
    }

    /**
     * @author Duncan Mills
     * @author Steve Muench
     * @param opList
     */
    public static void printOperationBindingExceptions(List opList) {
        if (opList != null && !opList.isEmpty()) {
            for (Object error : opList) {
                LOGGER.severe(error.toString());
            }
        }
    }
    
}
