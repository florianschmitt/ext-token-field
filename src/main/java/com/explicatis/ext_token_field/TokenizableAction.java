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

import com.vaadin.server.Resource;

public class TokenizableAction
{

	private final String	identifier;
	private String			label						= "";
	private int				viewOrder;
	public Resource			icon;
	private boolean			inheritsReadOnlyAndEnabled	= true;

	public TokenizableAction(String identifier, String label)
	{
		this.identifier = identifier;
		this.label = label;
	}

	public TokenizableAction(String identifier, String label, int viewOrder)
	{
		this(identifier, label);
		this.viewOrder = viewOrder;
	}

	public TokenizableAction(String identifier, Resource resource)
	{
		this.identifier = identifier;
		this.icon = resource;
	}

	public TokenizableAction(String identifier, int viewOrder, Resource resource)
	{
		this(identifier, resource);
		this.viewOrder = viewOrder;
	}

	public TokenizableAction(String identifier, String label, int viewOrder, Resource resource)
	{
		this(identifier, label, viewOrder);
		this.icon = resource;
	}

	public void onClick(Tokenizable tokenizable)
	{

	}

	public Resource getIcon()
	{
		return icon;
	}

	public void setIcon(Resource icon)
	{
		this.icon = icon;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getLabel()
	{
		return label;
	}

	public int getViewOrder()
	{
		return viewOrder;
	}

	public boolean getInheritsReadOnlyAndEnabled()
	{
		return inheritsReadOnlyAndEnabled;
	}

	public void setInheritsReadOnlyAndEnabled(boolean value)
	{
		this.inheritsReadOnlyAndEnabled = value;
	}
}
