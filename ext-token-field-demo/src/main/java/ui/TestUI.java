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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.explicatis.ext_token_field.TokenizableAction;
import com.explicatis.ext_token_field.events.TokenAddedEvent;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.ValidationResult;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
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

	private static String[]		LANGUAGES	= {"PHP", "Java", "JavaScript", "Scala", "Python", "C", "Ruby", "C++"};

	private VerticalLayout		notes;
	private VerticalLayout		mainLayout;
	private Registration		valueChangeListenerRegistration;
	private Set<Registration>	tokenChangeListeners;

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
		mainLayout = new VerticalLayout();

		Label heading = new Label("ExtTokenField");
		heading.addStyleName(ValoTheme.LABEL_HUGE);
		Label subheading = new Label(String.format("%d - Explicatis GmbH, Florian Schmitt", LocalDate.now().getYear()));
		subheading.addStyleName(ValoTheme.LABEL_TINY);

		VerticalLayout head = new VerticalLayout(heading, subheading);
		head.setMargin(new MarginInfo(false, true));
		head.setSpacing(false);

		notes = new VerticalLayout();
		notes.setSizeFull();
		notes.setMargin(new MarginInfo(false, true));
		notes.setSpacing(false);

		addNote("Keyboard controls (arrow-left, arrow-right & delete)");
		addNote("ComboBox or Button input (TextField support is planned)");
		addNote("Custom actions can be defined, if the setInheritsReadOnlyAndEnabled setting is false, they will be still be usable, when the ExtTokenField is set to read only or is not enabled");
		addNote("trims long token captions so that token is clickable to expose full caption (planned to be configurable, as to trimming length)");
		addNote("implement <b>Tokenizable</b> interface in your bean or entity class to be able to set the fields value as a List of these objects");

		mainLayout.addComponent(head);
		mainLayout.addComponent(notes);
		mainLayout.addComponent(new ConfigurableLayout());
		setContent(mainLayout);
	}

	private void addNote(String note)
	{
		Label l1 = new Label();
		l1.setIcon(VaadinIcons.CIRCLE);
		Label l2 = new Label(note, ContentMode.HTML);
		HorizontalLayout hl = new HorizontalLayout(l1, l2);
		hl.setMargin(false);
		hl.setHeight(10, Unit.PIXELS);
		hl.setComponentAlignment(l1, Alignment.MIDDLE_LEFT);
		hl.setComponentAlignment(l2, Alignment.MIDDLE_LEFT);
		notes.addComponent(hl);
	}

	private static long tokenCollectionIdDummy = 0;

	private static Collection<SimpleTokenizable> initTokenCollection()
	{
		tokenCollectionIdDummy = 0;

		return Stream.of(LANGUAGES)//
				.sorted()//
				.map(name -> new SimpleTokenizable(tokenCollectionIdDummy++, name))//
				.collect(Collectors.toList());
	}

	private static ComboBox<SimpleTokenizable> buildComboBox()
	{
		ComboBox<SimpleTokenizable> result = new ComboBox<>("", initTokenCollection());
		result.setItemCaptionGenerator(SimpleTokenizable::getStringValue);
		result.setPlaceholder("Type here to add");
		return result;
	}

	private Button buildAddButton()
	{
		Button result = new Button();
		result.setCaption("add element");
		result.setIcon(VaadinIcons.PLUS_CIRCLE);
		result.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		result.addClickListener(event -> notificate("add clicked"));
		return result;
	}

	private ValueChangeListener<SimpleTokenizable> getComboBoxValueChange(ExtTokenField extTokenField)
	{
		return event -> {
			SimpleTokenizable value = event.getValue();

			if (value != null)
			{
				extTokenField.addTokenizable(value);
				event.getSource().setValue(null);
			}
		};

	}

	private void addValueChangeListeners(ExtTokenField extTokenField)
	{
		valueChangeListenerRegistration = extTokenField.addValueChangeListener(event -> notificate("Value change: " + event.getValue()));
	}

	private void addTokenListeners(ExtTokenField extTokenField)
	{
		if (tokenChangeListeners == null)
		{
			tokenChangeListeners = new HashSet<>();
		}

		tokenChangeListeners.add(extTokenField.addTokenAddedListener(event -> notificate("Token added: " + event.getTokenizable().getStringValue())));
		tokenChangeListeners.add(extTokenField.addTokenRemovedListener(event -> notificate("Token removed: " + event.getTokenizable().getStringValue())));
		tokenChangeListeners.add(extTokenField.addTokenReorderedListener(
				event -> notificate(String.format("Token reordered: source=%s target=%s type=%s", event.getSourceTokenizable().getStringValue(), event.getTargetTokenizable().getStringValue(), event.getDropTargetType().toString()))));
	}

	private void removeValueChangeListener(ExtTokenField extTokenField)
	{
		if (valueChangeListenerRegistration != null)
		{
			valueChangeListenerRegistration.remove();
			valueChangeListenerRegistration = null;
		}
	}

	private void removeValueAddedAndRemovedListeners(ExtTokenField extTokenField)
	{
		if (tokenChangeListeners != null)
		{
			tokenChangeListeners.forEach(Registration::remove);
			tokenChangeListeners = null;
		}
	}

	protected void notificate(String msg)
	{
		Notification notification = new Notification(msg);
		notification.setDelayMsec(5000);
		notification.show(Page.getCurrent());
	}

	private class ConfigurableLayout extends VerticalLayout
	{

		private ExtTokenField				tokenField						= new ExtTokenField();
		private CheckBox					delete							= new CheckBox("activate or deactivate delete action", true);
		private CheckBox					comboBoxOrButton				= new CheckBox("ComboBox or Button");
		private CheckBox					readOnly						= new CheckBox("read only");
		private CheckBox					required						= new CheckBox("required", true);
		private CheckBox					enabled							= new CheckBox("enabled", true);
		private CheckBox					addCustomAction					= new CheckBox("add or remove custom action");
		private CheckBox					readOnlyIgnoringCustomAction	= new CheckBox("should the custom action ignore read only");
		private CheckBox					activateTokenListeners			= new CheckBox("add or remove TokenAddedListener & TokenRemovedListener & TokenReorderedListener");
		private CheckBox					activateValueChangeListener		= new CheckBox("add or remove ValueChangeListener");
		private CheckBox					enableDragDrop					= new CheckBox("enable drag and drop reordering");

		private ComboBox<SimpleTokenizable>	comboBox						= TestUI.buildComboBox();
		private Button						addButton						= buildAddButton();
		private Binder<DemoBean>			binder							= new Binder<>(DemoBean.class);

		public ConfigurableLayout()
		{
			binder.setBean(new DemoBean());
			initTokenField();
			bindTokenField();
			setSampleTokenizableValue();

			comboBox.addValueChangeListener(getComboBoxValueChange(tokenField));

			FormLayout formLayout = new FormLayout(tokenField);
			formLayout.setSizeFull();

			setMargin(new MarginInfo(false, true));
			addComponent(formLayout);
			FormLayout configLayout = new FormLayout(readOnly, enabled, required, delete, comboBoxOrButton, addCustomAction, readOnlyIgnoringCustomAction, activateValueChangeListener, activateTokenListeners, enableDragDrop);
			configLayout.setCaption("modify settings");
			configLayout.setSizeFull();
			addComponent(configLayout);
			setupCheckBoxes();
			initTestButtons();
		}

		private void bindTokenField()
		{
			binder.forField(tokenField)
					.asRequired("a value is required")
					.bind("tokens");
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
			tokenField.setInputField(comboBox);
			tokenField.setEnableDefaultDeleteTokenAction(delete.getValue());
		}

		private void setSampleTokenizableValue()
		{
			List<SimpleTokenizable> list = buildSampleTokenizableList();
			binder.setBean(new DemoBean(list));
		}

		private List<SimpleTokenizable> buildSampleTokenizableList()
		{
			List<SimpleTokenizable> result = new LinkedList<>();

			List<String> list = Stream.of(LANGUAGES)//
					.limit(LANGUAGES.length - 2)//
					.sorted()//
					.collect(Collectors.toList());

			for (int i = 0; i < list.size(); i++)
			{
				result.add(new SimpleTokenizable(i, list.get(i)));
			}

			return result;
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

			TokenizableAction tokenizableAction = new TokenizableAction("id1", VaadinIcons.VAADIN_V)
			{

				@Override
				public void onClick(Tokenizable token)
				{
					notificate("clicked " + token.getStringValue());
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

			activateTokenListeners.addValueChangeListener(event -> {
				if (tokenField.getListeners(TokenAddedEvent.class).isEmpty())
				{
					addTokenListeners(tokenField);
				}
				else
				{
					removeValueAddedAndRemovedListeners(tokenField);
				}
			});

			activateValueChangeListener.addValueChangeListener(event -> {
				if (valueChangeListenerRegistration == null)
				{
					addValueChangeListeners(tokenField);
				}
				else
				{
					removeValueChangeListener(tokenField);
				}
			});

			required.addValueChangeListener(e -> tokenField.setRequiredIndicatorVisible(required.getValue()));

			enableDragDrop.addValueChangeListener(e -> tokenField.setTokenDragDropEnabled(enableDragDrop.getValue()));
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
			BinderValidationStatus<DemoBean> status = binder.validate();

			if (status.hasErrors())
			{
				List<ValidationResult> errors = status.getValidationErrors();
				String msg = errors.stream()//
						.map(ValidationResult::getErrorMessage)//
						.collect(Collectors.joining(","));
				notificate("Error: " + msg);
			}
			else
			{
				notificate("Success");
			}
		}
	}
}
