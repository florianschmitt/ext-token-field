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

package com.explicatis.ext_token_field.shared;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.client.ExtTokenFieldWidget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(ExtTokenField.class)
public class ExtTokenFieldConnector extends AbstractSingleComponentContainerConnector
{

	@Override
	public void updateCaption(ComponentConnector connector)
	{

	}

	@Override
	public ExtTokenFieldWidget getWidget()
	{
		return (ExtTokenFieldWidget) super.getWidget();
	}

	@Override
	protected void init()
	{
		super.init();
	}

	@Override
	public ExtTokenFieldState getState()
	{
		return (ExtTokenFieldState) super.getState();
	}

	@Override
	public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event)
	{
	}

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent)
	{
		super.onStateChanged(stateChangeEvent);
	}
}
