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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.explicatis.ext_token_field.events.TokenAddedEvent;
import com.explicatis.ext_token_field.events.TokenAddedListener;
import com.explicatis.ext_token_field.events.TokenRemovedEvent;
import com.explicatis.ext_token_field.events.TokenRemovedListener;
import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.ExtTokenFieldState;
import com.explicatis.ext_token_field.shared.Token;
import com.explicatis.ext_token_field.shared.TokenAction;
import com.google.gwt.thirdparty.guava.common.collect.Iterators;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@SuppressWarnings("serial")
public class ExtTokenField extends AbstractField<List<? extends Tokenizable>> implements HasComponents
{

	private ExtTokenFieldServerRpc			serverRpc						= new ExtTokenFieldServerRpc()
																			{

																				@Override
																				public void tokenActionClicked(Token token, TokenAction tokenAction)
																				{
																					if (identifierToTokenizableAction.containsKey(tokenAction.identifier))
																					{
																						Tokenizable tokenizable = identifierToTokenizable.get(token.id);
																						identifierToTokenizableAction.get(tokenAction.identifier).onClick(tokenizable);
																					}
																				}
																			};

	private Map<Long, Tokenizable>			identifierToTokenizable			= new HashMap<Long, Tokenizable>();
	private Map<String, TokenizableAction>	identifierToTokenizableAction	= new HashMap<String, TokenizableAction>();

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

		addTokenAction(new DefaultDeleteTokenAction());
	}

	@Override
	protected void setInternalValue(List<? extends Tokenizable> newValue)
	{
		super.setInternalValue(newValue);

		identifierToTokenizable.clear();
		List<Token> newList = new ArrayList<Token>();

		for (Tokenizable t : newValue)
		{
			Token token = convertTokenizableToToken(t);
			identifierToTokenizable.put(t.getIdentifier(), t);
			newList.add(token);
		}

		getState().tokens = newList;
	}

	protected Token convertTokenizableToToken(Tokenizable value)
	{
		Token result = new Token();
		result.id = value.getIdentifier();
		result.value = value.getStringValue();
		return result;
	}

	public void addTokenAction(TokenizableAction tokenizableAction)
	{
		for (TokenAction ta : getState().tokenActions)
		{
			if (ta.identifier.equals(tokenizableAction.getIdentifier()))
			{
				throw new RuntimeException("TokenAction identifier is not unique");
			}
		}

		if (tokenizableAction.icon != null)
		{
			setResource(tokenizableAction.getIdentifier() + "-icon", tokenizableAction.icon);
		}

		TokenAction tokenAction = fromTokenizableActionToTokenAction(tokenizableAction);
		identifierToTokenizableAction.put(tokenizableAction.getIdentifier(), tokenizableAction);
		getState().tokenActions.add(tokenAction);
	}

	protected TokenAction fromTokenizableActionToTokenAction(TokenizableAction a)
	{
		TokenAction b = new TokenAction();
		b.identifier = a.getIdentifier();
		b.label = a.getLabel();
		b.viewOrder = a.getViewOrder();
		return b;
	}

	public void addTokenizable(Tokenizable tokenizable)
	{
		if (identifierToTokenizable.keySet().contains(tokenizable.getIdentifier()))
		{
			return;
		}

		Token token = convertTokenizableToToken(tokenizable);
		identifierToTokenizable.put(tokenizable.getIdentifier(), tokenizable);
		addToken(token);

		@SuppressWarnings("unchecked")
		List<Tokenizable> currentValue = (List<Tokenizable>) getValue();
		if (currentValue == null)
		{
			currentValue = new LinkedList<Tokenizable>();
		}
		currentValue.add(tokenizable);
		setValue(currentValue);

		fireEvent(new TokenAddedEvent(this, tokenizable));
	}

	public void removeTokenizable(Tokenizable tokenizable)
	{
		Token token = findTokenByTokenizable(tokenizable);
		removeToken(token);
		List<? extends Tokenizable> internalValue = getInternalValue();
		List<Tokenizable> newList = new LinkedList<Tokenizable>();
		for (Tokenizable t : internalValue)
		{
			if (t.getIdentifier() != tokenizable.getIdentifier())
			{
				newList.add(t);
			}
		}
		identifierToTokenizable.remove(token.id);
		setValue(newList);

		fireEvent(new TokenRemovedEvent(this, tokenizable));
	}

	protected Token findTokenByTokenizable(Tokenizable tokenizable)
	{
		for (Token token : getState().tokens)
		{
			if (token.id == tokenizable.getIdentifier())
			{
				return token;
			}
		}
		return null;
	}

	private void addToken(Token token)
	{
		getState().tokens.add(token);
	}

	private void removeToken(Token token)
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
	public Iterator<Component> iterator()
	{
		if (getInputField() == null)
			return Iterators.emptyIterator();

		return new ComponentIterator();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Class<? extends List<? extends Tokenizable>> getType()
	{
		return (Class) List.class;
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		super.setReadOnly(readOnly);
		getInputField().setVisible(readOnly);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		getInputField().setVisible(enabled);
	}

	public void addTokenAddedListener(TokenAddedListener listener)
	{
		addListener(TokenAddedEvent.class, listener, TokenAddedEvent.EVENT_METHOD);
	}

	public void removeTokenAddedListener(TokenAddedListener listener)
	{
		removeListener(TokenAddedEvent.class, listener, TokenAddedEvent.EVENT_METHOD);
	}

	public void addTokenRemovedListener(TokenRemovedListener listener)
	{
		addListener(TokenRemovedEvent.class, listener, TokenRemovedEvent.EVENT_METHOD);
	}

	public void removeTokenRemovedListener(TokenRemovedListener listener)
	{
		removeListener(TokenRemovedEvent.class, listener, TokenRemovedEvent.EVENT_METHOD);
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

	private class ComponentIterator implements Iterator<Component>, Serializable
	{

		boolean	first	= (getInputField() != null);

		@Override
		public boolean hasNext()
		{
			return first;
		}

		@Override
		public Component next()
		{
			first = false;
			return getInputField();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	public class DefaultDeleteTokenAction extends TokenizableAction
	{

		public DefaultDeleteTokenAction()
		{
			super(TokenAction.DELETE_TOKEN_ACTION_IDENTIFIER, Integer.MAX_VALUE, FontAwesome.MINUS_CIRCLE);
		}

		@Override
		public void onClick(Tokenizable tokenizable)
		{
			ExtTokenField.this.removeTokenizable(tokenizable);
		}
	}
}
