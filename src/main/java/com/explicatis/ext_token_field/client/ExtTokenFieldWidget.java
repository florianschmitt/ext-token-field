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

import java.util.LinkedList;
import java.util.List;

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.shared.Connector;

public class ExtTokenFieldWidget extends FlowPanel
{

	public static final String		TOKEN_FIELD_CLASS_NAME	= "exttokenfield";
	private List<TokenWidget>		tokenWidgets			= new LinkedList<>();
	private ExtTokenFieldServerRpc	serverRpc;

	public ExtTokenFieldWidget()
	{
		getElement().setClassName(TOKEN_FIELD_CLASS_NAME);
	}

	public void setInputField(Connector inputField)
	{
		if (inputField != null)
			add(((ComponentConnector) inputField).getWidget());
	}

	public void updateTokens(List<Token> tokens)
	{
		// TODO: register changes, not recreate everything
		removeAllToken();
		addTokens(tokens);
	}

	protected TokenWidget buildTokenWidget(Token token)
	{
		TokenWidget widget = new TokenWidget(token);
		widget.setServerRpc(serverRpc);
		return widget;
	}

	protected void addTokens(List<Token> tokens)
	{
		for (Token t : tokens)
		{
			TokenWidget widget = buildTokenWidget(t);
			insert(widget, 0);
			tokenWidgets.add(widget);
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

	public void setServerRpc(ExtTokenFieldServerRpc serverRpc)
	{
		this.serverRpc = serverRpc;
	}
}
