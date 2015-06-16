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

package com.explicatis.ext_token_field.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.Token;
import com.explicatis.ext_token_field.shared.TokenAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.Icon;
import com.vaadin.client.ui.VFilterSelect;
import com.vaadin.shared.Connector;

import elemental.events.KeyboardEvent.KeyCode;

public class ExtTokenFieldWidget extends FlowPanel
{

	public static final String			TOKEN_FIELD_CLASS_NAME	= "exttokenfield";

	private List<TokenWidget>			tokenWidgets			= new LinkedList<>();
	private ExtTokenFieldServerRpc		serverRpc;
	private VFilterSelect				inputFilterSelect;
	private Token						tokenToTheRight;
	private List<TokenAction>			tokenActions;
	private Map<TokenAction, String>	icons;
	private ApplicationConnection		applicationConnection;

	public ExtTokenFieldWidget()
	{
		getElement().setClassName(TOKEN_FIELD_CLASS_NAME);
	}

	public void setApplicationConnection(ApplicationConnection applicationConnection)
	{
		this.applicationConnection = applicationConnection;
	}

	public void setIconResourceUrl(TokenAction tokenAction, String url)
	{
		if (icons == null)
		{
			icons = new HashMap<>();
		}
		icons.put(tokenAction, url);
	}

	public void setTokenActions(Set<TokenAction> tokenActions)
	{
		List<TokenAction> sortedList = new LinkedList<>(tokenActions);
		Collections.sort(sortedList);
		this.tokenActions = sortedList;
	}

	public void setInputField(Connector inputField)
	{
		if (inputField != null)
		{
			inputFilterSelect = (VFilterSelect) ((ComponentConnector) inputField).getWidget();
			/**
			 * add key down handler, to select the token at the very left of the filter select when left key was pressed
			 * 
			 * TODO: more work to do... make sure suggestion box is closed etc.
			 */
			inputFilterSelect.tb.addKeyDownHandler(new KeyDownHandler()
			{

				@Override
				public void onKeyDown(KeyDownEvent event)
				{
					if (event.getNativeKeyCode() == KeyCode.LEFT)
					{
						if (ExtTokenFieldWidget.this.tokenWidgets.size() > 0)
						{
							tokenWidgets.get(tokenWidgets.size() - 1).setFocus(true);
						}
					}
				}
			});
			add(inputFilterSelect);
		}
	}

	public void updateTokens(List<Token> tokens)
	{
		// TODO: register changes, not recreate everything
		removeAllToken();
		addTokens(tokens);

		if (tokenToTheRight != null)
		{
			final TokenWidget tokenWidget = findTokenWidget(tokenToTheRight);
			Scheduler.get().scheduleDeferred(new ScheduledCommand()
			{

				@Override
				public void execute()
				{
					if (tokenWidget != null)
					{
						tokenWidget.setFocus(true);
					}
					tokenToTheRight = null;
				}
			});
		}
		else
		{
			// TODO: this should only happen after the very last token was deleted, not always...
			Scheduler.get().scheduleDeferred(new ScheduledCommand()
			{

				@Override
				public void execute()
				{
					inputFilterSelect.tb.setFocus(true);
				}
			});
		}
	}

	protected TokenWidget buildTokenWidget(final Token token)
	{
		final TokenWidget widget = new TokenWidget(token, tokenActions)
		{

			@Override
			protected void onTokenActionClicked(TokenAction tokenAction)
			{
				tokenActionClicked(this, tokenAction);
			}

			@Override
			protected void buildIcon(final TokenAction action, final Anchor actionAnchor)
			{
				if (icons != null && icons.containsKey(action))
				{
					Icon icon = applicationConnection.getIcon(icons.get(action));
					actionAnchor.getElement().insertBefore(icon.getElement(), null);
				}
			}
		};
		widget.addFocusHandler(new FocusHandler()
		{

			@Override
			public void onFocus(FocusEvent event)
			{
				widget.getElement().addClassName(TokenWidget.FOCUS_CLASS_NAME);
			}
		});

		widget.addBlurHandler(new BlurHandler()
		{

			@Override
			public void onBlur(BlurEvent event)
			{
				widget.getElement().removeClassName(TokenWidget.FOCUS_CLASS_NAME);
			}
		});

		widget.addKeyDownHandler(new KeyDownHandler()
		{

			@Override
			public void onKeyDown(KeyDownEvent event)
			{
				if (event.getNativeKeyCode() == KeyCodes.KEY_LEFT)
				{
					leftKeyDown(widget);
				}
				else if (event.getNativeKeyCode() == KeyCodes.KEY_RIGHT)
				{
					rightKeyDown(widget);
				}
				else if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE || event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
				{
					TokenAction deleteTokenAction = findTokenAction(TokenAction.DELETE_TOKEN_ACTION_IDENTIFIER);
					if (deleteTokenAction != null)
					{
						tokenActionClicked(widget, deleteTokenAction);
					}
				}
			}
		});
		return widget;
	}

	protected TokenAction findTokenAction(String identifier)
	{
		for (TokenAction action : tokenActions)
		{
			if (action.identifier.equals(identifier))
			{
				return action;
			}
		}
		return null;
	}

	protected void tokenActionClicked(final TokenWidget widget, final TokenAction tokenAction)
	{
		if (tokenAction.identifier.equals(TokenAction.DELETE_TOKEN_ACTION_IDENTIFIER))
		{
			TokenWidget tokenWidgetToTheRight = getTokenToTheRight(widget);
			if (tokenWidgetToTheRight != null)
			{
				tokenToTheRight = tokenWidgetToTheRight.getToken();
			}
		}

		serverRpc.tokenActionClicked(widget.getToken(), tokenAction);
	}

	protected void rightKeyDown(TokenWidget token)
	{
		TokenWidget tokenToTheRight = getTokenToTheRight(token);

		if (tokenToTheRight != null)
		{
			tokenToTheRight.setFocus(true);
		}
		else
		{
			inputFilterSelect.tb.setFocus(true);
		}
	}

	protected void leftKeyDown(TokenWidget token)
	{
		TokenWidget tokenToTheLeft = getTokenToTheLeft(token);
		if (tokenToTheLeft != null)
		{
			tokenToTheLeft.setFocus(true);
		}
	}

	protected TokenWidget getTokenToTheLeft(TokenWidget token)
	{
		if (!hasMoreTokensLeft(token))
		{
			return null;
		}
		int indexOf = tokenWidgets.indexOf(token);
		TokenWidget tokenWidget = tokenWidgets.get(indexOf - 1);
		return tokenWidget;
	}

	protected TokenWidget getTokenToTheRight(TokenWidget token)
	{
		if (!hasMoreTokensRight(token))
		{
			return null;
		}
		int indexOf = tokenWidgets.indexOf(token);
		TokenWidget tokenWidget = tokenWidgets.get(indexOf + 1);
		return tokenWidget;
	}

	protected boolean hasMoreTokensLeft(TokenWidget token)
	{
		int indexOf = tokenWidgets.indexOf(token);
		return indexOf > 0;
	}

	protected boolean hasMoreTokensRight(TokenWidget token)
	{
		int indexOf = tokenWidgets.indexOf(token);
		return indexOf < tokenWidgets.size() - 1;
	}

	protected void addTokens(List<Token> tokens)
	{
		for (int i = 0; i < tokens.size(); i++)
		{
			Token t = tokens.get(i);
			TokenWidget widget = buildTokenWidget(t);
			tokenWidgets.add(widget);
			insert(widget, i);
		}
	}

	protected void removeAllToken()
	{
		for (TokenWidget t : tokenWidgets)
		{
			remove(t);
		}
		tokenWidgets.clear();
	}

	private TokenWidget findTokenWidget(Token token)
	{
		for (TokenWidget t : tokenWidgets)
		{
			if (t.getToken().equals(token))
			{
				return t;
			}
		}

		return null;
	}

	public void setServerRpc(ExtTokenFieldServerRpc serverRpc)
	{
		this.serverRpc = serverRpc;
	}
}
