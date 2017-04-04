package org.yuanheng.cookcc.parser.ast;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class ProductionParserTest
{
	private void testCase (ProductionParser parser, String rule, String output) throws IOException
	{
		SymbolLibrary lib = new DummySymbolLib ();
		ArrayList<Symbol> symbols = parser.parse (lib, 1, rule);
		Assert.assertEquals (output, AbstractSymbol.toString (symbols));
	}

	private void testErrorCase (ProductionParser parser, String rule) throws IOException
	{
		SymbolLibrary lib = new DummySymbolLib ();
		Assert.assertNull (parser.parse (lib, 1, rule));
	}

	@Test
	public void test () throws IOException
	{
		ProductionParser parser = new ProductionParser ();
		testCase (parser, "a b", "a b");
		testCase (parser, "a (b) c (d)", "a (b) c (d)");
		testCase (parser, "a b +", "a b+");
		testCase (parser, "a ( b ) +", "a b+");
		testCase (parser, "(a b)", "a b");
		testCase (parser, "(a b)+", "(a b)+");
		testCase (parser, "(a b)*", "(a b)*");
		testCase (parser, "(a b)?", "(a b)?");
		testCase (parser, "a ( b c d ) +", "a (b c d)+");
		testCase (parser, "a ( b c d ) *", "a (b c d)*");
		testCase (parser, "a ( b c d ) ?", "a (b c d)?");
		testCase (parser, "a ( b c d ) * (e f g)", "a (b c d)* (e f g)");
	}

	@Test
	public void testChar () throws IOException
	{
		ProductionParser parser = new ProductionParser ();
		testCase (parser, "'a' b", "'a' b");
		testCase (parser, "'\\t' b", "'\\t' b");
		testCase (parser, "'\\\\' b", "'\\\\' b");
		testCase (parser, "'-' b", "'-' b");
		testCase (parser, "('-')+", "'-'+");
	}

	@Test
	public void testOr () throws IOException
	{
		ProductionParser parser = new ProductionParser ();
		testCase (parser, "a | b", "(a | b)");
		testCase (parser, "a (a | b)", "a (a | b)");
		testCase (parser, "a (a | b) c", "a (a | b) c");
		testCase (parser, "a (a|b c| d)c", "a (a | b c | d) c");
		testCase (parser, "a (a|b c| d)?c", "a (a | b c | d)? c");
		testCase (parser, "(a | b)", "(a | b)");
		testCase (parser, "(a | b | c | d)", "(a | b | c | d)");
		testCase (parser, "(a | b) +", "(a | b)+");
		testCase (parser, "(a | b | c | d) *", "(a | b | c | d)*");
		testCase (parser, "(a | b* | c+ d+) +", "(a | b* | c+ d+)+");
		testCase (parser, "(a | b (c d)+)", "(a | b (c d)+)");
	}

	@Test
	public void testError () throws IOException
	{
		ProductionParser parser = new ProductionParser ();
		testErrorCase (parser, "a (b");
		testErrorCase (parser, "a ) b");
		testErrorCase (parser, "a ( b ) + *");

		testErrorCase (parser, "(a | b | )");
	}
}
