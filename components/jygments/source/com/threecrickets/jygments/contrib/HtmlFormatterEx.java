package com.threecrickets.jygments.contrib;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.Util;
import com.threecrickets.jygments.format.Formatter;
import com.threecrickets.jygments.grammar.Token;
import com.threecrickets.jygments.grammar.TokenType;
import com.threecrickets.jygments.style.ColorStyleElement;
import com.threecrickets.jygments.style.EffectStyleElement;
import com.threecrickets.jygments.style.FontStyleElement;
import com.threecrickets.jygments.style.Style;
import com.threecrickets.jygments.style.StyleElement;

public class HtmlFormatterEx extends Formatter {

	public HtmlFormatterEx() throws ResolutionException {
		this(Style.getByName("default"), false, null);
	}

	public HtmlFormatterEx(Style style, boolean full, String encoding) {
		super(style, full, null, encoding);
	}

	@Override
	public void format(Iterable<Token> tokenSource, Writer writer) throws IOException {
		writer.write("<div><pre><code>\n");
		StringBuilder line = new StringBuilder();
		List<String> lineList = new ArrayList<String>();
		for(Token token : tokenSource) {
			String[] toks = token.getValue().split("\n", -1);
			for(int i = 0; i < toks.length - 1; ++i) {
				format_partial_token(token, toks[i], line);
				lineList.add(line.toString());
				line = new StringBuilder();
			}
			format_partial_token(token, toks[toks.length - 1], line);
		}
		if(line.length() > 0) {
			lineList.add(line.toString());		
		}
		int line_no = 1;
		for(String formattedLine : lineList) {
			format_line(formattedLine, writer, line_no++, lineList.size());
		}
		writer.write("</code></pre></div>\n");
		writer.flush();
	}

    private void format_partial_token(Token token, String part_tok, StringBuilder line)
    {	
		if( token.getType().getShortName().length() > 0 )
		{
			line.append( "<span class=\"" );
			line.append( token.getType().getShortName() );
			line.append( "\">" );
			line.append( Util.escapeHtml( part_tok ) );
			line.append( "</span>" );
		}
		else
			line.append( Util.escapeHtml( part_tok ) );
    }

	private void format_line(String line, Writer writer, int lineNumber, int maxLines) throws IOException {
		int len = String.valueOf(maxLines).length();
		String number = String.valueOf(lineNumber);
		int appendixSize = len - number.length();
		for( int i = 0; i < appendixSize; ++i)
			number = " " + number;
		writer.write("<span class=\"lineno\">" + number + "</span>");
        writer.write(line);
        writer.write("\n");
	}
	

	public void getStyleSheet(Writer writer) throws IOException {
		writer.write("<style type=\"text/css\">\n");
		writer.write("    .highlight pre {\n");
		writer.write("        word-wrap: normal;\n");
		writer.write("    }\n");
		writer.write("    .highlight pre code {\n");
		writer.write("        white-space: pre;\n");
		writer.write("    }\n");
		writer.write("    span.lineno { background-color: #f0f0f0; padding: 0 5px 0 5px; }\n    pre { line-height: 125%; }\n");
		formatStyleSheet(writer);
		writer.write("</style>\n");
		writer.flush();
	}

	private void formatStyleSheet( Writer writer ) throws IOException
	{
		for( Map.Entry<TokenType, List<StyleElement>> entry : getStyle().getStyleElements().entrySet() )
		{
			TokenType tokenType = entry.getKey();
			List<StyleElement> styleElementsForTokenType = entry.getValue();
			writer.write( "    ." );
			writer.write( tokenType.getShortName() );
			writer.write( " { " );
			for( StyleElement styleElement : styleElementsForTokenType )
			{
				if( styleElement instanceof ColorStyleElement )
				{
					ColorStyleElement colorStyleElement = (ColorStyleElement) styleElement;
					if( colorStyleElement.getType() == ColorStyleElement.Type.Foreground )
						writer.write( "color: " );
					else if( colorStyleElement.getType() == ColorStyleElement.Type.Background )
						writer.write( "background-color: " );
					else if( colorStyleElement.getType() == ColorStyleElement.Type.Border )
						writer.write( "border: 1px solid " );
					writer.write( colorStyleElement.getColor() );
					writer.write( "; " );
				}
				else if( styleElement instanceof EffectStyleElement )
				{
					if( styleElement == EffectStyleElement.Bold )
						writer.write( "font-weight: bold; " );
					else if( styleElement == EffectStyleElement.Italic )
						writer.write( "font-style: italic; " );
					else if( styleElement == EffectStyleElement.Underline )
						writer.write( "text-decoration: underline; " );
				}
				else if( styleElement instanceof FontStyleElement )
				{
					// We don't want to set fonts in this formatter
				}
			}
			writer.write( "} /* " );
			writer.write( tokenType.getName() );
			writer.write( " */\n" );
		}
	}
}
