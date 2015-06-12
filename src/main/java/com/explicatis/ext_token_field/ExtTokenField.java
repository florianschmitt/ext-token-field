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

package com.explicatis.ext_token_field;

import java.util.Iterator;
import java.util.stream.Stream;

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.ExtTokenFieldState;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.thirdparty.guava.common.collect.Iterators;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class ExtTokenField extends AbstractComponentContainer
{

	private ExtTokenFieldServerRpc	serverRpc	= new ExtTokenFieldServerRpc()
												{

													@Override
													public void tokenDeleteClicked(Token token)
													{
														removeToken(token);
													}
												};

	public ExtTokenField()
	{
		registerRpc(serverRpc);
		addAttachListener(new AttachListener()
		{

			@Override
			public void attach(AttachEvent event)
			{
				if (getInputField() == null)
					throw new RuntimeException("no input field set");
			}
		});
	}

	public void addToken(Token token)
	{
		getState().tokens.add(token);
	}

	public void removeToken(Token token)
	{
		getState().tokens.remove(token);
	}

	public void setInputField(ComboBox field)
	{
		addComponent(field);
		getState().inputField = field;
	}

	public ComboBox getInputField()
	{
		return (ComboBox) getState().inputField;
	}

	@Override
	protected ExtTokenFieldState getState()
	{
		return (ExtTokenFieldState) super.getState();
	}

	@Override
	public void replaceComponent(Component oldComponent, Component newComponent)
	{
	}

	@Override
	public int getComponentCount()
	{
		return getInputField() == null ? 0 : 1;
	}

	@Override
	public Iterator<Component> iterator()
	{
		if (getInputField() == null)
			return Iterators.emptyIterator();
		return Stream.of((Component) getInputField()).iterator();
	}
}
