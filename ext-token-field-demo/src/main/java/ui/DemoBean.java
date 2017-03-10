package ui;

import java.util.List;

import com.explicatis.ext_token_field.SimpleTokenizable;

public class DemoBean
{

	private List<SimpleTokenizable> tokens;

	public DemoBean()
	{

	}

	public DemoBean(List<SimpleTokenizable> tokens)
	{
		setTokens(tokens);
	}

	public List<SimpleTokenizable> getTokens()
	{
		return tokens;
	}

	public void setTokens(List<SimpleTokenizable> tokens)
	{
		this.tokens = tokens;
	}
}
