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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.ExtTokenFieldState;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.thirdparty.guava.common.collect.Iterators;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@SuppressWarnings("serial")
public class ExtTokenField extends AbstractField<List<? extends Tokenizable>> implements HasComponents// , Editor
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

	private Map<Token, Tokenizable>	map	= new HashMap<>();

	@Override
	protected void setInternalValue(List<? extends Tokenizable> newValue)
	{
		super.setInternalValue(newValue);

		removeAllToken();
		map.clear();

		for (Tokenizable t : newValue)
		{
			Token token = convertTokenizableToToken(t);
			addToken(token);
			map.put(token, t);
		}
	}

	protected Token convertTokenizableToToken(Tokenizable value)
	{
		Token result = new Token();
		result.id = value.getIdentifier();
		result.value = value.getStringValue();
		return result;
	}

	private void addToken(Token token)
	{
		getState().tokens.add(token);
	}

	private void removeToken(Token token)
	{
		getState().tokens.remove(token);

		Tokenizable tokenizable = map.get(token);
		List<? extends Tokenizable> value2 = getValue();
		value2.remove(tokenizable);
		setValue(value2);
	}

	private void removeAllToken()
	{
		getState().tokens.clear();
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
	public Iterator<Component> iterator()
	{
		if (getInputField() == null)
			return Iterators.emptyIterator();
		return Stream.of((Component) getInputField()).iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends List<? extends Tokenizable>> getType()
	{
		return (Class<? extends List<? extends Tokenizable>>) List.class;
	}

	/**
	 * copied from AbstractComponentContainer
	 * 
	 * @param c
	 */
	public void addComponent(Component c)
	{
		// Make sure we're not adding the component inside it's own content
		if (isOrHasAncestor(c))
		{
			throw new IllegalArgumentException("Component cannot be added inside it's own content");
		}

		if (c.getParent() != null)
		{
			// If the component already has a parent, try to remove it
			AbstractSingleComponentContainer.removeFromParent(c);
		}

		c.setParent(this);
		fireComponentAttachEvent(c);
		markAsDirty();
	}

	/**
	 * copied from AbstractComponentContainer
	 * 
	 */
	protected void fireComponentAttachEvent(Component component)
	{
		fireEvent(new ComponentAttachEvent(this, component));
	}
}
