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

package com.explicatis.ext_token_field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.explicatis.ext_token_field.events.TokenAddedEvent;
import com.explicatis.ext_token_field.events.TokenAddedListener;
import com.explicatis.ext_token_field.events.TokenRemovedEvent;
import com.explicatis.ext_token_field.events.TokenRemovedListener;
import com.explicatis.ext_token_field.events.TokenReorderedEvent;
import com.explicatis.ext_token_field.events.TokenReorderedListener;
import com.explicatis.ext_token_field.shared.DropTargetType;
import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.ExtTokenFieldState;
import com.explicatis.ext_token_field.shared.Token;
import com.explicatis.ext_token_field.shared.TokenAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Button;
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

																				@Override
																				public void tokenDroped(Token sourceToken, Token targetToken, DropTargetType type)
																				{
																					handleDroppedToken(sourceToken, targetToken, type);
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
				if (!hasInputButton() && !hasInputField())
					throw new RuntimeException("no input field nor input button set");
			}
		});
	}

	public void setTokenDragDropEnabled(boolean value)
	{
		getState().tokenDragAndDropEnabled = value;
	}

	public void setEnableDefaultDeleteTokenAction(boolean value)
	{
		DefaultDeleteTokenAction defaultDeleteTokenAction = new DefaultDeleteTokenAction();

		if (value)
		{
			if (!hasTokenizableAction(defaultDeleteTokenAction))
				addTokenAction(defaultDeleteTokenAction);
		}
		else
		{
			if (hasTokenizableAction(defaultDeleteTokenAction))
				removeTokenizableAction(defaultDeleteTokenAction);
		}
	}

	@Override
	protected void setInternalValue(List<? extends Tokenizable> newValue)
	{
		super.setInternalValue(newValue);

		identifierToTokenizable.clear();
		List<Token> newList = new ArrayList<Token>();

		if (newValue != null && newValue.size() > 0)
		{
			for (Tokenizable t : newValue)
			{
				Token token = convertTokenizableToToken(t);
				identifierToTokenizable.put(t.getIdentifier(), t);
				newList.add(token);
			}
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

	public void removeTokenizableAction(TokenizableAction tokenizableAction)
	{
		boolean containsKey = identifierToTokenizableAction.containsKey(tokenizableAction.getIdentifier());
		if (!containsKey)
		{
			throw new RuntimeException("TokenizableAction with identifier " + tokenizableAction.getIdentifier() + " not found");
		}

		TokenAction toRemove = findTokenActionByTokenizableAction(tokenizableAction);
		getState().tokenActions.remove(toRemove);
		identifierToTokenizableAction.remove(tokenizableAction.getIdentifier());
	}

	protected TokenAction fromTokenizableActionToTokenAction(TokenizableAction a)
	{
		TokenAction b = new TokenAction();
		b.identifier = a.getIdentifier();
		b.label = a.getLabel();
		b.viewOrder = a.getViewOrder();
		b.inheritsReadOnlyAndEnabled = a.getInheritsReadOnlyAndEnabled();
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
		fireEvent(new ValueChangeEvent(this));
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
		// valueChangeEvent doesn't need to be fired, because a new list is created
	}

	protected void handleDroppedToken(Token sourceToken, Token targetToken, DropTargetType type)
	{
		reorderToken(sourceToken, targetToken, type);

		@SuppressWarnings("unchecked")
		List<Tokenizable> currentValue = (List<Tokenizable>) getValue();
		if (currentValue == null)
		{
			throw new IllegalStateException("value cannot be null, if token was dropped");
		}

		Tokenizable sourceTokenizable = findTokenizableInListByToken(currentValue, sourceToken);
		Tokenizable targetTokenizable = findTokenizableInListByToken(currentValue, targetToken);

		int targetIndex = currentValue.indexOf(targetTokenizable);
		targetIndex = DropTargetType.BEFORE.equals(type) ? targetIndex : targetIndex + 1;

		boolean afterLast = targetIndex == currentValue.size() && DropTargetType.AFTER.equals(type);

		if (isIndexOfALowerThanB(currentValue, sourceTokenizable, targetTokenizable))
			targetIndex--;

		currentValue.remove(sourceTokenizable);
		if (afterLast)
		{
			currentValue.add(sourceTokenizable);
		}
		else
		{
			currentValue.add(targetIndex, sourceTokenizable);
		}

		setValue(currentValue);

		fireEvent(new TokenReorderedEvent(this, sourceTokenizable, targetTokenizable, type));
		fireEvent(new ValueChangeEvent(this));
	}

	private Tokenizable findTokenizableInListByToken(List<Tokenizable> list, Token token)
	{
		for (Tokenizable tokenizable : list)
		{
			if (tokenizable.getIdentifier() == token.id)
			{
				return tokenizable;
			}
		}
		return null;
	}

	public boolean hasTokenizableAction(TokenizableAction tokenizableAction)
	{
		return findTokenActionByTokenizableAction(tokenizableAction) != null;
	}

	protected TokenAction findTokenActionByTokenizableAction(TokenizableAction tokenizableAction)
	{
		for (TokenAction tokenAction : getState().tokenActions)
		{
			if (tokenAction.identifier.equals(tokenizableAction.getIdentifier()))
			{
				return tokenAction;
			}
		}
		return null;
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

	private void reorderToken(Token source, Token target, DropTargetType type)
	{
		List<Token> tokens = getState().tokens;
		int targetIndex = tokens.indexOf(target);
		targetIndex = DropTargetType.BEFORE.equals(type) ? targetIndex : targetIndex + 1;

		boolean afterLast = targetIndex == tokens.size() && DropTargetType.AFTER.equals(type);

		if (isIndexOfALowerThanB(tokens, source, target))
			targetIndex--;

		tokens.remove(source);
		if (afterLast)
		{
			tokens.add(source);
		}
		else
		{
			tokens.add(targetIndex, source);
		}

		getState().tokens = tokens;
	}

	public void setInputField(ComboBox field)
	{
		if (field != null)
		{
			removeFieldOrButton();
			addComponent(field);
			getState().inputField = field;
		}
	}

	public void setInputButton(Button button)
	{
		if (button != null)
		{
			removeFieldOrButton();
			addComponent(button);
			getState().inputButton = button;
		}
	}

	public boolean hasInputField()
	{
		return getInputField() != null;
	}

	public boolean hasInputButton()
	{
		return getInputButton() != null;
	}

	private void removeFieldOrButton()
	{
		if (iterator().hasNext())
		{
			removeComponent(iterator().next());
			getState().inputButton = null;
			getState().inputField = null;
		}
	}

	public ComboBox getInputField()
	{
		return (ComboBox) getState().inputField;
	}

	public Button getInputButton()
	{
		return (Button) getState().inputButton;
	}

	@Override
	protected ExtTokenFieldState getState()
	{
		return (ExtTokenFieldState) super.getState();
	}

	@Override
	public Iterator<Component> iterator()
	{
		if (getInputField() == null && getInputButton() == null)
			return emptyIterator();

		return new ComponentIterator();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Class<? extends List<? extends Tokenizable>> getType()
	{
		return (Class) List.class;
	}

	protected Component internalGetInputComponentOrNull()
	{
		if (hasInputField())
			return getInputField();
		else if (hasInputButton())
			return getInputButton();
		else
			return null;
	}

	protected void updateComponentVisibleState()
	{
		boolean readOnly = isReadOnly();
		boolean enabled = isEnabled();
		Component componentOrNull = internalGetInputComponentOrNull();
		if (componentOrNull != null)
		{
			componentOrNull.setVisible(enabled && !readOnly);
		}
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		super.setReadOnly(readOnly);
		updateComponentVisibleState();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		updateComponentVisibleState();
	}

	@Override
	public void focus()
	{
		Component componentOrNull = internalGetInputComponentOrNull();
		if (componentOrNull != null)
		{
			if (Focusable.class.isInstance(componentOrNull))
			{
				Focusable focusable = (Focusable) componentOrNull;
				focusable.focus();
			}
		}
	}

	@Override
	public boolean isEmpty()
	{
		@SuppressWarnings("unchecked")
		List<Tokenizable> currentValue = (List<Tokenizable>) getValue();
		if (currentValue == null)
		{
			return true;
		}
		return currentValue.isEmpty();
	}

	private static <V> boolean isIndexOfALowerThanB(List<V> list, V a, V b)
	{
		int indexOfA = list.indexOf(a);
		int indexOfB = list.indexOf(b);

		return indexOfA < indexOfB;
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

	public void addTokenReorderedListener(TokenReorderedListener listener)
	{
		addListener(TokenReorderedEvent.class, listener, TokenReorderedEvent.EVENT_METHOD);
	}

	public void removeTokenReorderedListener(TokenReorderedListener listener)
	{
		removeListener(TokenReorderedEvent.class, listener, TokenReorderedEvent.EVENT_METHOD);
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
	public void removeComponent(Component c)
	{
		if (equals(c.getParent()))
		{
			c.setParent(null);
			fireComponentDetachEvent(c);
			markAsDirty();
		}
	}

	/**
	 * copied from AbstractComponentContainer
	 * 
	 */
	protected void fireComponentAttachEvent(Component component)
	{
		fireEvent(new ComponentAttachEvent(this, component));
	}

	/**
	 * copied from AbstractComponentContainer
	 * 
	 */
	protected void fireComponentDetachEvent(Component component)
	{
		fireEvent(new ComponentDetachEvent(this, component));
	}

	/**
	 * copied from AbstractComponentContainer
	 * 
	 */
	private class ComponentIterator implements Iterator<Component>, Serializable
	{

		boolean first = (hasInputButton()) || (hasInputField());

		@Override
		public boolean hasNext()
		{
			return first;
		}

		@Override
		public Component next()
		{
			first = false;
			if (hasInputField())
				return getInputField();
			else if (hasInputButton())
				return getInputButton();
			return null;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> emptyIterator()
	{
		return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
	}

	private static class EmptyIterator<E> implements Iterator<E>
	{

		static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator<Object>();

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public E next()
		{
			throw new NoSuchElementException();
		}

		@Override
		public void remove()
		{
			throw new IllegalStateException();
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
