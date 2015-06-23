/*
 * Copyright 2015 Florian Schmitt, Explicatis GmbH <florian.schmitt@explicatis.com>
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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import data.MyCustomBean;

@SuppressWarnings("serial")
@SpringUI
@Theme(ValoTheme.THEME_NAME)
@Widgetset(value = "com.explicatis.ext_token_field.WidgetSet")
public class TestUI extends UI
{

	private VerticalLayout	mainLayout;

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
		mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		mainLayout.addComponent(getTestLayout1());
		mainLayout.addComponent(getTestLayout2());
		mainLayout.addComponent(getTestLayout3());
		mainLayout.addComponent(getTestLayout4());
		mainLayout.addComponent(getTestLayout5());
		mainLayout.addComponent(getTestLayout6());
		mainLayout.addComponent(getTestLayout7());

		setContent(mainLayout);
	}

	private HorizontalLayout getTestLayout1()
	{
		HorizontalLayout result = new HorizontalLayout();
		ComboBox b = new ComboBox();
		b.setInputPrompt("Type here");
		b.addContainerProperty("label", String.class, "");
		Object addItem = b.addItem();
		b.getItem(addItem).getItemProperty("label").setValue("test");
		b.setItemCaptionPropertyId("label");

		ExtTokenField f = new ExtTokenField();
		f.setInputField(b);
		mainLayout.addComponent(f);

		b.addValueChangeListener(new ValueChangeListener()
		{

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				Object id = event.getProperty().getValue();
				if (id != null)
				{
					Item item = b.getItem(id);
					String string = (String) item.getItemProperty("label").getValue();

					SimpleTokenizable t = new SimpleTokenizable(1, string);
					f.addTokenizable(t);
					b.setValue(null);
					// @SuppressWarnings("unchecked")
					// List<Tokenizable> value = (List<Tokenizable>) f.getValue();
					// if (value == null)
					// {
					// value = new LinkedList<>();
					// }
					// value.add(t);
					// f.setValue(value);
				}
			}
		});
		result.addComponent(f);
		return result;
	}

	private HorizontalLayout getTestLayout2()
	{
		HorizontalLayout result = new HorizontalLayout();

		ComboBox b2 = new ComboBox();
		// b2.setWidth(100, Unit.PERCENTAGE);
		b2.setInputPrompt("Type here");

		ExtTokenField f2 = new ExtTokenField();
		f2.setInputField(b2);
		mainLayout.addComponent(f2);

		List<SimpleTokenizable> list = new LinkedList<>();
		list.add(new SimpleTokenizable(123l, "ein Text"));
		list.add(new SimpleTokenizable(124l, "ein sehr sehr sehr sehr sehr sehr sehr sehr sehr sehr langer Text"));

		f2.setValue(list);
		result.addComponent(f2);
		return result;
	}

	private HorizontalLayout getTestLayout3()
	{
		HorizontalLayout result = new HorizontalLayout();

		List<MyCustomBean> list = new LinkedList<>();
		list.add(new MyCustomBean(111, "Wert 1"));
		list.add(new MyCustomBean(112, "Wert 2"));
		list.add(new MyCustomBean(113, "Wert 3"));
		list.add(new MyCustomBean(114, "Wert 4 asdf"));

		ComboBox combo = new ComboBox();
		combo.setNullSelectionAllowed(false);
		combo.setInputPrompt("Type here");
		combo.addContainerProperty("label", String.class, "");
		Object addItem = combo.addItem();
		combo.getItem(addItem).getItemProperty("label").setValue("Test 1");
		addItem = combo.addItem();
		combo.getItem(addItem).getItemProperty("label").setValue("Test 2");
		addItem = combo.addItem();
		combo.getItem(addItem).getItemProperty("label").setValue("Test 3");
		combo.setItemCaptionPropertyId("label");

		ExtTokenField tokenField = new ExtTokenField();
		tokenField.setInputField(combo);
		result.addComponent(tokenField);
		tokenField.setValue(list);

		combo.addValueChangeListener(new ValueChangeListener()
		{

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				Object id = event.getProperty().getValue();
				if (id != null)
				{
					Item item = combo.getItem(id);
					String string = (String) item.getItemProperty("label").getValue();

					int intId = (int) id;

					SimpleTokenizable t = new SimpleTokenizable((long) intId, string);
					tokenField.addTokenizable(t);
					combo.setValue(null);
				}
			}
		});

		tokenField.addTokenAddedListener(new TokenAddedListener()
		{

			@Override
			public void tokenAddedEvent(TokenAddedEvent event)
			{
				Notification.show("Token added: " + event.getTokenizable().getStringValue());
			}
		});

		tokenField.addTokenRemovedListener(new TokenRemovedListener()
		{

			@Override
			public void tokenRemovedEvent(TokenRemovedEvent event)
			{
				Notification.show("Token removed: " + event.getTokenizable().getStringValue());
			}
		});

		return result;
	}

	private HorizontalLayout getTestLayout4()
	{
		HorizontalLayout result = new HorizontalLayout();

		List<MyCustomBean> list = new LinkedList<>();
		list.add(new MyCustomBean(1, "Wert 1"));
		list.add(new MyCustomBean(2, "Wert 2"));
		list.add(new MyCustomBean(3, "Wert 3"));
		list.add(new MyCustomBean(4, "Wert 4"));

		ComboBox combo = new ComboBox();
		combo.setSizeFull();
		combo.setInputPrompt("Type here");

		ExtTokenField tokenField = new ExtTokenField();
		tokenField.setSizeFull();
		tokenField.setInputField(combo);
		result.addComponent(tokenField);

		tokenField.setValue(list);

		tokenField.addValueChangeListener(new ValueChangeListener()
		{

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				Notification.show("Value changed: " + printValue(tokenField.getValue()));
			}
		});

		result.setSizeFull();

		return result;
	}

	private HorizontalLayout getTestLayout5()
	{
		HorizontalLayout result = new HorizontalLayout();

		List<MyCustomBean> list = new LinkedList<>();
		list.add(new MyCustomBean(1, "Wert 1"));
		list.add(new MyCustomBean(2, "Wert 2"));
		list.add(new MyCustomBean(3, "Wert 3"));
		list.add(new MyCustomBean(4, "Wert 4"));

		ComboBox combo = new ComboBox();
		combo.setSizeFull();
		combo.setDescription("TESTTESTTEST");
		combo.setInputPrompt("Type here");

		ExtTokenField tokenField = new ExtTokenField();

		TokenizableAction b = new TokenizableAction("id1", FontAwesome.ADJUST)
		{

			public void onClick(com.explicatis.ext_token_field.shared.Token token)
			{
				Notification.show("clicked");
			};
		};
		tokenField.addTokenAction(b);

		tokenField.setSizeFull();
		tokenField.setInputField(combo);
		tokenField.setValue(list);
		tokenField.addValueChangeListener(new ValueChangeListener()
		{

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				Notification.show("Value changed: " + printValue(tokenField.getValue()));
			}
		});

		result.addComponent(tokenField);
		result.setSizeFull();

		return result;
	}

	private HorizontalLayout getTestLayout6()
	{
		HorizontalLayout result = new HorizontalLayout();

		List<MyCustomBean> list = new LinkedList<>();
		list.add(new MyCustomBean(1, "Wert 1"));
		list.add(new MyCustomBean(2, "Wert 2"));
		list.add(new MyCustomBean(3, "Wert 3"));
		list.add(new MyCustomBean(4, "Wert 4"));

		ComboBox combo = new ComboBox();
		combo.setSizeFull();
		combo.setDescription("TESTTESTTEST");
		combo.setInputPrompt("Type here");

		ExtTokenField tokenField = new ExtTokenField();

		TokenizableAction b = new TokenizableAction("id1", FontAwesome.ADJUST)
		{

			@Override
			public void onClick(com.explicatis.ext_token_field.Tokenizable token)
			{
				Notification.show("clicked");
			};
		};
		b.setInheritsReadOnlyAndEnabled(false);
		tokenField.addTokenAction(b);
		tokenField.setSizeFull();
		tokenField.setInputField(combo);
		tokenField.setValue(list);
		tokenField.setReadOnly(true);
		result.addComponent(tokenField);
		result.setSizeFull();

		return result;
	}

	private HorizontalLayout getTestLayout7()
	{
		HorizontalLayout result = new HorizontalLayout();

		List<MyCustomBean> list = new LinkedList<>();
		list.add(new MyCustomBean(1, "Wert 1"));
		list.add(new MyCustomBean(2, "Wert 2"));
		list.add(new MyCustomBean(3, "Wert 3"));
		list.add(new MyCustomBean(4, "Wert 4"));

		ExtTokenField tokenField = new ExtTokenField();

		tokenField.setSizeFull();
		Button add = new Button();
		add.setCaption("add element");
		add.setIcon(FontAwesome.PLUS_CIRCLE);
		add.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		add.addClickListener(new ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				Notification.show("add clicked");
			}
		});
		tokenField.setInputButton(add);
		tokenField.setValue(list);
		result.addComponent(tokenField);
		result.setSizeFull();

		return result;
	}

	private String printValue(List<? extends Tokenizable> list)
	{
		StringBuilder result = new StringBuilder();
		if (list != null)
		{
			result.append("[");

			for (Tokenizable t : list)
			{
				result.append(String.format("(%d, %s)", t.getIdentifier(), t.getStringValue()));
			}

			result.append("]");

			return result.toString();
		}

		return "null";
	}
}
