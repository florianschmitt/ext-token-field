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
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
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

		ExtTokenField f = new ExtTokenField();
		mainLayout.addComponent(f);

		setContent(mainLayout);
	}
}
