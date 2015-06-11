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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author fschmitt
 *
 */
@SuppressWarnings("deprecation")
public class TokenWidget extends Widget
{

	public static final String	TOKEN_CLASS_NAME		= "token";
	public static final String	TOKEN_LABEL_CLASS_NAME	= "token-label";
	public static final String	TOKEN_REMOVE_CLASS_NAME	= "token-remove";

	private Element				labelSpan;

	public TokenWidget()
	{
		DivElement rootElement = DivElement.as(DOM.createDiv());
		rootElement.setClassName(TOKEN_CLASS_NAME);

		labelSpan = DOM.createSpan();
		labelSpan.setClassName(TOKEN_LABEL_CLASS_NAME);
		rootElement.appendChild(labelSpan);

		Element removeAnchor = DOM.createAnchor();
		removeAnchor.setClassName(TOKEN_REMOVE_CLASS_NAME);
		removeAnchor.setInnerText("×");
		rootElement.appendChild(removeAnchor);

		setElement(rootElement);
	}

	public void setLabel(String labelText)
	{
		labelSpan.setInnerText(labelText);
	}
}
