/*
 * Copyright 2015 Explicatis GmbH <ext-token-field@explicatis.com>
 * 
 * Author: Florian Schmitt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ui;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.explicatis.ext_token_field.TokenizableAction;
import com.explicatis.ext_token_field.events.TokenAddedEvent;
import com.explicatis.ext_token_field.events.TokenAddedListener;
import com.explicatis.ext_token_field.events.TokenRemovedEvent;
import com.explicatis.ext_token_field.events.TokenRemovedListener;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@Theme(ValoTheme.THEME_NAME)
@Widgetset(value = "widgetset.WidgetSet")
@SpringUI
public class TestUI extends UI
{

	private static final String	LABEL		= "label";
	private static String[]		LANGUAGES	= {"PHP", "Java", "JavaScript", "Scala", "Python", "C", "Ruby", "C++"};

	private VerticalLayout		notes;
	private VerticalLayout		mainLayout;

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
		mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		Label heading = new Label("ExtTokenField");
		heading.addStyleName(ValoTheme.LABEL_HUGE);
		Label subheading = new Label(String.format("%d - Explicatis GmbH, Florian Schmitt", LocalDate.now().getYear()));
		subheading.addStyleName(ValoTheme.LABEL_TINY);

		VerticalLayout head = new VerticalLayout(heading, subheading);

		notes = new VerticalLayout();

		addNote("Keyboard controls (arrow-left, arrow-right & delete)");
		addNote("ComboBox or Button input (TextField support is planned)");
		addNote("Custom actions can be defined, if the setInheritsReadOnlyAndEnabled setting is false, they will be still be usable, when the ExtTokenField is set to read only or is not enabled");
		addNote("trims long token captions so that token is clickable to expose full caption (planned to be configurable, as to trimming length)");
		addNote("implement <b>Tokenizable</b> interface in your bean or entity class to be able to set the fields value as a List of these objects");

		notes.setSizeFull();
		mainLayout.addComponent(head);
		mainLayout.addComponent(notes);
		mainLayout.addComponent(new ConfigurableLayout());
		setContent(mainLayout);
	}

	private void addNote(String note)
	{
		Label l1 = new Label();
		l1.setIcon(FontAwesome.CHECK_SQUARE);
		Label l2 = new Label(note, ContentMode.HTML);
		HorizontalLayout hl = new HorizontalLayout(l1, l2);
		hl.setHeight(10, Unit.PIXELS);
		hl.setSpacing(true);
		notes.addComponent(hl);
	}

	@SuppressWarnings("unchecked")
	private static ComboBox buildComboBox()
	{
		ComboBox result = new ComboBox();
		result.setItemCaptionPropertyId(LABEL);
		result.setInputPrompt("Type here to add");
		result.addContainerProperty(LABEL, String.class, "");

		for (String lang : LANGUAGES)
		{
			Object addItem = result.addItem();
			result.getItem(addItem).getItemProperty(LABEL).setValue(lang);
		}

		return result;
	}

	private static Button buildAddButton()
	{
		Button result = new Button();
		result.setCaption("add element");
		result.setIcon(FontAwesome.PLUS_CIRCLE);
		result.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		result.addClickListener(event -> Notification.show("add clicked"));
		return result;
	}

	private ValueChangeListener getComboBoxValueChange(ExtTokenField extTokenField, ComboBox comboBox)
	{
		return event -> {
			Object id = event.getProperty().getValue();
			if (id != null)
			{
				Item item = comboBox.getItem(id);
				String string = (String) item.getItemProperty(LABEL).getValue();
				int idInt = (int) id;
				SimpleTokenizable t = new SimpleTokenizable(Integer.toUnsignedLong(idInt), string);
				extTokenField.addTokenizable(t);

				// if you would use a real container, you would filter the selected tokens out

				// reset combobox
				comboBox.setValue(null);
			}
		};
	}

	private void addValueChangeListeners(ExtTokenField extTokenField)
	{
		extTokenField.addValueChangeListener(event -> Notification.show("Value change: " + event.getProperty().getValue()));
	}

	private void addValueAddedAndRemovedListeners(ExtTokenField extTokenField)
	{
		extTokenField.addTokenAddedListener(event -> Notification.show("Token added: " + event.getTokenizable().getStringValue()));
		extTokenField.addTokenRemovedListener(event -> Notification.show("Token removed: " + event.getTokenizable().getStringValue()));
	}

	private void removeValueChangeListener(ExtTokenField extTokenField)
	{
		Collection<?> valueChange = extTokenField.getListeners(ValueChangeEvent.class);
		if (valueChange != null && !valueChange.isEmpty())
		{
			extTokenField.removeValueChangeListener((ValueChangeListener) valueChange.iterator().next());
		}
	}

	private void removeValueAddedAndRemovedListeners(ExtTokenField extTokenField)
	{
		Collection<?> listenerAdded = extTokenField.getListeners(TokenAddedEvent.class);
		if (listenerAdded != null && !listenerAdded.isEmpty())
		{
			extTokenField.removeTokenAddedListener((TokenAddedListener) listenerAdded.iterator().next());
		}

		Collection<?> listenerRemoved = extTokenField.getListeners(TokenRemovedEvent.class);
		if (listenerRemoved != null && !listenerRemoved.isEmpty())
		{
			extTokenField.removeTokenRemovedListener((TokenRemovedListener) listenerRemoved.iterator().next());
		}
	}

	private class ConfigurableLayout extends VerticalLayout
	{

		private ExtTokenField	tokenField						= new ExtTokenField();
		private CheckBox		delete							= new CheckBox("activate or deactivate delete action", true);
		private CheckBox		comboBoxOrButton				= new CheckBox("ComboBox or Button");
		private CheckBox		readOnly						= new CheckBox("read only");
		private CheckBox		required						= new CheckBox("required");
		private CheckBox		enabled							= new CheckBox("enabled", true);
		private CheckBox		addCustomAction					= new CheckBox("add or remove custom action");
		private CheckBox		readOnlyIgnoringCustomAction	= new CheckBox("should the custom action ignore read only");
		private CheckBox		activateTokenAddedListener		= new CheckBox("add or remove TokenAddedListener & TokenRemovedListener");
		private CheckBox		activateValueChangeListener		= new CheckBox("add or remove ValueChangeListener");

		private ComboBox		comboBox						= TestUI.buildComboBox();
		private Button			addButton						= TestUI.buildAddButton();

		public ConfigurableLayout()
		{
			initTokenField();

			comboBox.addValueChangeListener(getComboBoxValueChange(tokenField, comboBox));

			FormLayout formLayout = new FormLayout(tokenField);
			formLayout.setSizeFull();

			addComponent(formLayout);
			FormLayout configLayout = new FormLayout(readOnly, enabled, required, delete, comboBoxOrButton, addCustomAction, readOnlyIgnoringCustomAction, activateValueChangeListener, activateTokenAddedListener);
			configLayout.setCaption("modify settings");
			configLayout.setSizeFull();
			addComponent(configLayout);
			setupCheckBoxes();
			initTestButtons();
		}

		private void initTestButtons()
		{
			Button focusTestButton = initFocusTestButton();
			Button validateTestButton = initValidateTestButton();
			HorizontalLayout btnLayout = new HorizontalLayout(focusTestButton, validateTestButton);
			btnLayout.setSpacing(true);
			addComponent(btnLayout);
		}

		private void initTokenField()
		{
			tokenField.setCaption("Tokens");
			tokenField.setRequiredError("a value is required");
			tokenField.setInputField(comboBox);
			tokenField.setEnableDefaultDeleteTokenAction(delete.getValue());

			List<Tokenizable> list = buildSampleTokenizableList();
			tokenField.setValue(list);
		}

		private List<Tokenizable> buildSampleTokenizableList()
		{
			List<Tokenizable> list = new LinkedList<>();
			for (int i = 0; i < LANGUAGES.length - 2; i++)
			{
				list.add(new SimpleTokenizable(i, LANGUAGES[i]));
			}
			return list;
		}

		private void setupCheckBoxes()
		{
			delete.addValueChangeListener(event -> tokenField.setEnableDefaultDeleteTokenAction(delete.getValue()));

			readOnly.addValueChangeListener(event -> tokenField.setReadOnly(!tokenField.isReadOnly()));

			enabled.addValueChangeListener(event -> tokenField.setEnabled(!tokenField.isEnabled()));

			comboBoxOrButton.addValueChangeListener(event -> {
				if (tokenField.hasInputField())
					tokenField.setInputButton(addButton);
				else
					tokenField.setInputField(comboBox);
			});

			TokenizableAction tokenizableAction = new TokenizableAction("id1", FontAwesome.GEARS)
			{

				@Override
				public void onClick(Tokenizable token)
				{
					Notification.show("clicked " + token.getStringValue());
				};
			};

			addCustomAction.addValueChangeListener(event -> {
				if (tokenField.hasTokenizableAction(tokenizableAction))
				{
					tokenField.removeTokenizableAction(tokenizableAction);
				}
				else
				{
					tokenizableAction.setInheritsReadOnlyAndEnabled(!readOnlyIgnoringCustomAction.getValue());
					tokenField.addTokenAction(tokenizableAction);
				}
			});

			readOnlyIgnoringCustomAction.addValueChangeListener(event -> {
				if (tokenField.hasTokenizableAction(tokenizableAction))
				{
					tokenField.removeTokenizableAction(tokenizableAction);
					tokenizableAction.setInheritsReadOnlyAndEnabled(!readOnlyIgnoringCustomAction.getValue());
					tokenField.addTokenAction(tokenizableAction);
				}
			});

			activateTokenAddedListener.addValueChangeListener(event -> {
				if (tokenField.getListeners(TokenAddedEvent.class).isEmpty())
				{
					addValueAddedAndRemovedListeners(tokenField);
				}
				else
				{
					removeValueAddedAndRemovedListeners(tokenField);
				}
			});

			activateValueChangeListener.addValueChangeListener(event -> {
				if (tokenField.getListeners(ValueChangeEvent.class).isEmpty())
				{
					addValueChangeListeners(tokenField);
				}
				else
				{
					removeValueChangeListener(tokenField);
				}
			});

			required.addValueChangeListener(e -> tokenField.setRequired(required.getValue()));
		}

		private Button initFocusTestButton()
		{
			Button result = new Button("focus field", this::focusField);
			return result;
		}

		private Button initValidateTestButton()
		{
			Button result = new Button("validate field", this::validateField);
			return result;
		}

		private void focusField(ClickEvent event)
		{
			tokenField.focus();
		}

		private void validateField(ClickEvent event)
		{
			try
			{
				tokenField.validate();
				Notification.show("Success");
			}
			catch (InvalidValueException e)
			{
				Notification.show("Error: " + e.getMessage());
			}
		}
	}
}
