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

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.shared.Token;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

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
		mainLayout.addComponent(new Label("Vaadin button:"));

		ComboBox b = new ComboBox();
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
					Token token = new Token();
					token.id = 123l;
					token.value = string;
					f.addToken(token);
				}
			}
		});

		ComboBox b2 = new ComboBox();

		ExtTokenField f2 = new ExtTokenField();
		f2.setInputField(b2);
		mainLayout.addComponent(f2);

		Token token2 = new Token();
		token2.id = 123l;
		token2.value = "ein Text";
		f2.addToken(token2);

		Token token3 = new Token();
		token3.id = 124l;
		token3.value = "ein sehr sehr sehr sehr sehr sehr sehr sehr sehr sehr langer Text";
		f2.addToken(token3);

		Token token4 = new Token();
		token4.id = 125l;
		token4.value = "ein Text";
		f2.addToken(token4);

		Token token5 = new Token();
		token5.id = 126l;
		token5.value = "ein Text";
		f2.addToken(token5);

		Token token6 = new Token();
		token6.id = 127l;
		token6.value = "ein Text";
		f2.addToken(token6);

		Token token7 = new Token();
		token7.id = 128l;
		token7.value = "ein Text";
		f2.addToken(token7);

		setContent(mainLayout);

	}
}
