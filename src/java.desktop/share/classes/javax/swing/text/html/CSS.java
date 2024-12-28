/*
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.swing.text.html;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.SizeRequirements;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.View;

/**
 * Defines a set of
 * <a href="http://www.w3.org/TR/REC-CSS1">CSS attributes</a>
 * as a typesafe enumeration.  The HTML View implementations use
 * CSS attributes to determine how they will render. This also defines
 * methods to map between CSS/HTML/StyleConstants. Any shorthand
 * properties, such as font, are mapped to the intrinsic properties.
 * <p>The following describes the CSS properties that are supported by the
 * rendering engine:
 * <ul><li>font-family
 *   <li>font-style
 *   <li>font-size (supports relative units)
 *   <li>font-weight
 *   <li>font
 *   <li>color
 *   <li>background-color (with the exception of transparent)
 *   <li>background-image
 *   <li>background-repeat
 *   <li>background-position
 *   <li>background
 *   <li>text-decoration (with the exception of blink and overline)
 *   <li>vertical-align (only sup and super)
 *   <li>text-align (justify is treated as center)
 *   <li>margin-top
 *   <li>margin-right
 *   <li>margin-bottom
 *   <li>margin-left
 *   <li>margin
 *   <li>padding-top
 *   <li>padding-right
 *   <li>padding-bottom
 *   <li>padding-left
 *   <li>padding
 *   <li>border-top-style
 *   <li>border-right-style
 *   <li>border-bottom-style
 *   <li>border-left-style
 *   <li>border-style (only supports inset, outset and none)
 *   <li>border-top-color
 *   <li>border-right-color
 *   <li>border-bottom-color
 *   <li>border-left-color
 *   <li>border-color
 *   <li>list-style-image
 *   <li>list-style-type
 *   <li>list-style-position
 * </ul>
 * The following are modeled, but currently not rendered.
 * <ul><li>font-variant
 *   <li>background-attachment (background always treated as scroll)
 *   <li>word-spacing
 *   <li>letter-spacing
 *   <li>text-indent
 *   <li>text-transform
 *   <li>line-height
 *   <li>border-top-width (this is used to indicate if a border should be used)
 *   <li>border-right-width
 *   <li>border-bottom-width
 *   <li>border-left-width
 *   <li>border-width
 *   <li>border-top
 *   <li>border-right
 *   <li>border-bottom
 *   <li>border-left
 *   <li>border
 *   <li>width
 *   <li>height
 *   <li>float
 *   <li>clear
 *   <li>display
 *   <li>white-space
 *   <li>list-style
 * </ul>
 * <p><b>Note: for the time being we do not fully support relative units,
 * unless noted, so that
 * p { margin-top: 10% } will be treated as if no margin-top was specified.
 * Additionally, the named, hexadecimal and rgb or rgba color notations from
 * CSS 4 are supported.</b>
 *
 * @spec https://www.w3.org/TR/REC-CSS1 Cascading Style Sheets, level 1
 * @spec https://www.w3.org/TR/css-color-4/#rgb-functions The RGB functions
 * @spec https://www.w3.org/TR/css-color-4/#hex-notation The RGB Hexadecimal Notations
 * @spec https://www.w3.org/TR/css-color-4/#named-colors Named Colors
 * @author  Timothy Prinzing
 * @author  Scott Violet
 * @see StyleSheet
 */
@SuppressWarnings("serial") // Same-version serialization only
public class CSS implements Serializable {

    /**
     * Definitions to be used as a key on AttributeSet's
     * that might hold CSS attributes.  Since this is a
     * closed set (i.e. defined exactly by the specification),
     * it is final and cannot be extended.
     */
    public static final class Attribute {

        private Attribute(String name, String defaultValue, boolean inherited) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.inherited = inherited;
        }

        /**
         * The string representation of the attribute.  This
         * should exactly match the string specified in the
         * CSS specification.
         */
        public String toString() {
            return name;
        }

        /**
         * Fetch the default value for the attribute.
         * If there is no default value (such as for
         * composite attributes), null will be returned.
         *
         * @return default value for the attribute
         */
        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * Indicates if the attribute should be inherited
         * from the parent or not.
         *
         * @return true if the attribute should be inherited from the parent
         */
        public boolean isInherited() {
            return inherited;
        }

        private String name;
        private String defaultValue;
        private boolean inherited;


        /**
         * CSS attribute "background".
         */
        public static final Attribute BACKGROUND =
            new Attribute("background", null, false);

        /**
         * CSS attribute "background-attachment".
         */
        public static final Attribute BACKGROUND_ATTACHMENT =
            new Attribute("background-attachment", "scroll", false);

        /**
         * CSS attribute "background-color".
         */
        public static final Attribute BACKGROUND_COLOR =
            new Attribute("background-color", "transparent", false);

        /**
         * CSS attribute "background-image".
         */
        public static final Attribute BACKGROUND_IMAGE =
            new Attribute("background-image", "none", false);

        /**
         * CSS attribute "background-position".
         */
        public static final Attribute BACKGROUND_POSITION =
            new Attribute("background-position", "0% 0%", false);

        /**
         * CSS attribute "background-repeat".
         */
        public static final Attribute BACKGROUND_REPEAT =
            new Attribute("background-repeat", "repeat", false);

        /**
         * CSS attribute "border".
         */
        public static final Attribute BORDER =
            new Attribute("border", null, false);

        /**
         * CSS attribute "border-bottom".
         */
        public static final Attribute BORDER_BOTTOM =
            new Attribute("border-bottom", null, false);

        /**
         * CSS attribute "border-bottom-color".
         */
        public static final Attribute BORDER_BOTTOM_COLOR =
            new Attribute("border-bottom-color", null, false);

        /**
         * CSS attribute "border-bottom-style".
         */
        public static final Attribute BORDER_BOTTOM_STYLE =
            new Attribute("border-bottom-style", "none", false);

        /**
         * CSS attribute "border-bottom-width".
         */
        public static final Attribute BORDER_BOTTOM_WIDTH =
            new Attribute("border-bottom-width", "medium", false);

        /**
         * CSS attribute "border-color".
         */
        public static final Attribute BORDER_COLOR =
            new Attribute("border-color", null, false);

        /**
         * CSS attribute "border-left".
         */
        public static final Attribute BORDER_LEFT =
            new Attribute("border-left", null, false);

        /**
         * CSS attribute "margin-right".
         */
        public static final Attribute BORDER_LEFT_COLOR =
            new Attribute("border-left-color", null, false);

        /**
         * CSS attribute "border-left-style".
         */
        public static final Attribute BORDER_LEFT_STYLE =
            new Attribute("border-left-style", "none", false);

        /**
         * CSS attribute "border-left-width".
         */
        public static final Attribute BORDER_LEFT_WIDTH =
            new Attribute("border-left-width", "medium", false);

        /**
         * CSS attribute "border-right".
         */
        public static final Attribute BORDER_RIGHT =
            new Attribute("border-right", null, false);

        /**
         * CSS attribute "border-right-color".
         */
        public static final Attribute BORDER_RIGHT_COLOR =
            new Attribute("border-right-color", null, false);

        /**
         * CSS attribute "border-right-style".
         */
        public static final Attribute BORDER_RIGHT_STYLE =
            new Attribute("border-right-style", "none", false);

        /**
         * CSS attribute "border-right-width".
         */
        public static final Attribute BORDER_RIGHT_WIDTH =
            new Attribute("border-right-width", "medium", false);

        /**
         * CSS attribute "border-style".
         */
        public static final Attribute BORDER_STYLE =
            new Attribute("border-style", "none", false);

        /**
         * CSS attribute "border-top".
         */
        public static final Attribute BORDER_TOP =
            new Attribute("border-top", null, false);

        /**
         * CSS attribute "border-top-color".
         */
        public static final Attribute BORDER_TOP_COLOR =
            new Attribute("border-top-color", null, false);

        /**
         * CSS attribute "border-top-style".
         */
        public static final Attribute BORDER_TOP_STYLE =
            new Attribute("border-top-style", "none", false);

        /**
         * CSS attribute "border-top-width".
         */
        public static final Attribute BORDER_TOP_WIDTH =
            new Attribute("border-top-width", "medium", false);

        /**
         * CSS attribute "border-width".
         */
        public static final Attribute BORDER_WIDTH =
            new Attribute("border-width", "medium", false);

        /**
         * CSS attribute "clear".
         */
        public static final Attribute CLEAR =
            new Attribute("clear", "none", false);

        /**
         * CSS attribute "color".
         */
        public static final Attribute COLOR =
            new Attribute("color", "black", true);

        /**
         * CSS attribute "display".
         */
        public static final Attribute DISPLAY =
            new Attribute("display", "block", false);

        /**
         * CSS attribute "float".
         */
        public static final Attribute FLOAT =
            new Attribute("float", "none", false);

        /**
         * CSS attribute "font".
         */
        public static final Attribute FONT =
            new Attribute("font", null, true);

        /**
         * CSS attribute "font-family".
         */
        public static final Attribute FONT_FAMILY =
            new Attribute("font-family", null, true);

        /**
         * CSS attribute "font-size".
         */
        public static final Attribute FONT_SIZE =
            new Attribute("font-size", "medium", true);

        /**
         * CSS attribute "font-style".
         */
        public static final Attribute FONT_STYLE =
            new Attribute("font-style", "normal", true);

        /**
         * CSS attribute "font-variant".
         */
        public static final Attribute FONT_VARIANT =
            new Attribute("font-variant", "normal", true);

        /**
         * CSS attribute "font-weight".
         */
        public static final Attribute FONT_WEIGHT =
            new Attribute("font-weight", "normal", true);

        /**
         * CSS attribute "height".
         */
        public static final Attribute HEIGHT =
            new Attribute("height", "auto", false);

        /**
         * CSS attribute "letter-spacing".
         */
        public static final Attribute LETTER_SPACING =
            new Attribute("letter-spacing", "normal", true);

        /**
         * CSS attribute "line-height".
         */
        public static final Attribute LINE_HEIGHT =
            new Attribute("line-height", "normal", true);

        /**
         * CSS attribute "list-style".
         */
        public static final Attribute LIST_STYLE =
            new Attribute("list-style", null, true);

        /**
         * CSS attribute "list-style-image".
         */
        public static final Attribute LIST_STYLE_IMAGE =
            new Attribute("list-style-image", "none", true);

        /**
         * CSS attribute "list-style-position".
         */
        public static final Attribute LIST_STYLE_POSITION =
            new Attribute("list-style-position", "outside", true);

        /**
         * CSS attribute "list-style-type".
         */
        public static final Attribute LIST_STYLE_TYPE =
            new Attribute("list-style-type", "disc", true);

        /**
         * CSS attribute "margin".
         */
        public static final Attribute MARGIN =
            new Attribute("margin", null, false);

        /**
         * CSS attribute "margin-bottom".
         */
        public static final Attribute MARGIN_BOTTOM =
            new Attribute("margin-bottom", "0", false);

        /**
         * CSS attribute "margin-left".
         */
        public static final Attribute MARGIN_LEFT =
            new Attribute("margin-left", "0", false);

        /**
         * CSS attribute "margin-right".
         */
        public static final Attribute MARGIN_RIGHT =
            new Attribute("margin-right", "0", false);

        /*
         * made up css attributes to describe orientation depended
         * margins. used for <dir>, <menu>, <ul> etc. see
         * 5088268 for more details
         */
        static final Attribute MARGIN_LEFT_LTR =
            new Attribute("margin-left-ltr",
                          Integer.toString(Integer.MIN_VALUE), false);

        static final Attribute MARGIN_LEFT_RTL =
            new Attribute("margin-left-rtl",
                          Integer.toString(Integer.MIN_VALUE), false);

        static final Attribute MARGIN_RIGHT_LTR =
            new Attribute("margin-right-ltr",
                          Integer.toString(Integer.MIN_VALUE), false);

        static final Attribute MARGIN_RIGHT_RTL =
            new Attribute("margin-right-rtl",
                          Integer.toString(Integer.MIN_VALUE), false);


        /**
         * CSS attribute "margin-top".
         */
        public static final Attribute MARGIN_TOP =
            new Attribute("margin-top", "0", false);

        /**
         * CSS attribute "padding".
         */
        public static final Attribute PADDING =
            new Attribute("padding", null, false);

        /**
         * CSS attribute "padding-bottom".
         */
        public static final Attribute PADDING_BOTTOM =
            new Attribute("padding-bottom", "0", false);

        /**
         * CSS attribute "padding-left".
         */
        public static final Attribute PADDING_LEFT =
            new Attribute("padding-left", "0", false);

        /**
         * CSS attribute "padding-right".
         */
        public static final Attribute PADDING_RIGHT =
            new Attribute("padding-right", "0", false);

        /**
         * CSS attribute "padding-top".
         */
        public static final Attribute PADDING_TOP =
            new Attribute("padding-top", "0", false);

        /**
         * CSS attribute "text-align".
         */
        public static final Attribute TEXT_ALIGN =
            new Attribute("text-align", null, true);

        /**
         * CSS attribute "text-decoration".
         */
        public static final Attribute TEXT_DECORATION =
            new Attribute("text-decoration", "none", true);

        /**
         * CSS attribute "text-indent".
         */
        public static final Attribute TEXT_INDENT =
            new Attribute("text-indent", "0", true);

        /**
         * CSS attribute "text-transform".
         */
        public static final Attribute TEXT_TRANSFORM =
            new Attribute("text-transform", "none", true);

        /**
         * CSS attribute "vertical-align".
         */
        public static final Attribute VERTICAL_ALIGN =
            new Attribute("vertical-align", "baseline", false);

        /**
         * CSS attribute "word-spacing".
         */
        public static final Attribute WORD_SPACING =
            new Attribute("word-spacing", "normal", true);

        /**
         * CSS attribute "white-space".
         */
        public static final Attribute WHITE_SPACE =
            new Attribute("white-space", "normal", true);

        /**
         * CSS attribute "width".
         */
        public static final Attribute WIDTH =
            new Attribute("width", "auto", false);

        /*public*/ static final Attribute BORDER_SPACING =
            new Attribute("border-spacing", "0", true);

        /*public*/ static final Attribute CAPTION_SIDE =
            new Attribute("caption-side", "left", true);

        // All possible CSS attribute keys.
        static final Attribute[] allAttributes = {
            BACKGROUND, BACKGROUND_ATTACHMENT, BACKGROUND_COLOR,
            BACKGROUND_IMAGE, BACKGROUND_POSITION, BACKGROUND_REPEAT,
            BORDER, BORDER_BOTTOM, BORDER_BOTTOM_WIDTH, BORDER_COLOR,
            BORDER_LEFT, BORDER_LEFT_WIDTH, BORDER_RIGHT, BORDER_RIGHT_WIDTH,
            BORDER_STYLE, BORDER_TOP, BORDER_TOP_WIDTH, BORDER_WIDTH,
            BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE,
            BORDER_LEFT_STYLE,
            BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR,
            BORDER_LEFT_COLOR,
            CLEAR, COLOR, DISPLAY, FLOAT, FONT, FONT_FAMILY, FONT_SIZE,
            FONT_STYLE, FONT_VARIANT, FONT_WEIGHT, HEIGHT, LETTER_SPACING,
            LINE_HEIGHT, LIST_STYLE, LIST_STYLE_IMAGE, LIST_STYLE_POSITION,
            LIST_STYLE_TYPE, MARGIN, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT,
            MARGIN_TOP, PADDING, PADDING_BOTTOM, PADDING_LEFT, PADDING_RIGHT,
            PADDING_TOP, TEXT_ALIGN, TEXT_DECORATION, TEXT_INDENT, TEXT_TRANSFORM,
            VERTICAL_ALIGN, WORD_SPACING, WHITE_SPACE, WIDTH,
            BORDER_SPACING, CAPTION_SIDE,
            MARGIN_LEFT_LTR, MARGIN_LEFT_RTL, MARGIN_RIGHT_LTR, MARGIN_RIGHT_RTL
        };

        private static final Attribute[] ALL_MARGINS =
                { MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM, MARGIN_LEFT };
        private static final Attribute[] ALL_PADDING =
                { PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, PADDING_LEFT };
        private static final Attribute[] ALL_BORDER_WIDTHS =
                { BORDER_TOP_WIDTH, BORDER_RIGHT_WIDTH, BORDER_BOTTOM_WIDTH,
                  BORDER_LEFT_WIDTH };
        private static final Attribute[] ALL_BORDER_STYLES =
                { BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE,
                  BORDER_LEFT_STYLE };
        private static final Attribute[] ALL_BORDER_COLORS =
                { BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR,
                  BORDER_LEFT_COLOR };

    }

    static final class Value {

        private Value(String name) {
            this.name = name;
        }

        /**
         * The string representation of the attribute.  This
         * should exactly match the string specified in the
         * CSS specification.
         */
        public String toString() {
            return name;
        }

        static final Value INHERITED = new Value("inherited");
        static final Value NONE = new Value("none");
        static final Value HIDDEN = new Value("hidden");
        static final Value DOTTED = new Value("dotted");
        static final Value DASHED = new Value("dashed");
        static final Value SOLID = new Value("solid");
        static final Value DOUBLE = new Value("double");
        static final Value GROOVE = new Value("groove");
        static final Value RIDGE = new Value("ridge");
        static final Value INSET = new Value("inset");
        static final Value OUTSET = new Value("outset");
        // Lists.
        static final Value DISC = new Value("disc");
        static final Value CIRCLE = new Value("circle");
        static final Value SQUARE = new Value("square");
        static final Value DECIMAL = new Value("decimal");
        static final Value LOWER_ROMAN = new Value("lower-roman");
        static final Value UPPER_ROMAN = new Value("upper-roman");
        static final Value LOWER_ALPHA = new Value("lower-alpha");
        static final Value UPPER_ALPHA = new Value("upper-alpha");
        // background-repeat
        static final Value BACKGROUND_NO_REPEAT = new Value("no-repeat");
        static final Value BACKGROUND_REPEAT = new Value("repeat");
        static final Value BACKGROUND_REPEAT_X = new Value("repeat-x");
        static final Value BACKGROUND_REPEAT_Y = new Value("repeat-y");
        // background-attachment
        static final Value BACKGROUND_SCROLL = new Value("scroll");
        static final Value BACKGROUND_FIXED = new Value("fixed");

        private String name;

        static final Value[] allValues = {
            INHERITED, NONE, DOTTED, DASHED, SOLID, DOUBLE, GROOVE,
            RIDGE, INSET, OUTSET, DISC, CIRCLE, SQUARE, DECIMAL,
            LOWER_ROMAN, UPPER_ROMAN, LOWER_ALPHA, UPPER_ALPHA,
            BACKGROUND_NO_REPEAT, BACKGROUND_REPEAT,
            BACKGROUND_REPEAT_X, BACKGROUND_REPEAT_Y,
            BACKGROUND_FIXED, BACKGROUND_FIXED
        };
    }

    /**
     * Constructs a CSS object.
     */
    public CSS() {
        baseFontSize = baseFontSizeIndex + 1;
        // setup the css conversion table
        valueConvertor = new Hashtable<Object, Object>();
        valueConvertor.put(CSS.Attribute.FONT_SIZE, new FontSize());
        valueConvertor.put(CSS.Attribute.FONT_FAMILY, new FontFamily());
        valueConvertor.put(CSS.Attribute.FONT_WEIGHT, new FontWeight());
        Object bs = new BorderStyle();
        valueConvertor.put(CSS.Attribute.BORDER_TOP_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_STYLE, bs);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_STYLE, bs);
        Object cv = new ColorValue();
        valueConvertor.put(CSS.Attribute.COLOR, cv);
        valueConvertor.put(CSS.Attribute.BACKGROUND_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_TOP_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_COLOR, cv);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_COLOR, cv);
        Object lv = new LengthValue();
        valueConvertor.put(CSS.Attribute.MARGIN_TOP, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_BOTTOM, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT_LTR, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_LEFT_RTL, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT_LTR, lv);
        valueConvertor.put(CSS.Attribute.MARGIN_RIGHT_RTL, lv);
        valueConvertor.put(CSS.Attribute.PADDING_TOP, lv);
        valueConvertor.put(CSS.Attribute.PADDING_BOTTOM, lv);
        valueConvertor.put(CSS.Attribute.PADDING_LEFT, lv);
        valueConvertor.put(CSS.Attribute.PADDING_RIGHT, lv);
        Object bv = new BorderWidthValue(null, 0);
        valueConvertor.put(CSS.Attribute.BORDER_TOP_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_BOTTOM_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_LEFT_WIDTH, bv);
        valueConvertor.put(CSS.Attribute.BORDER_RIGHT_WIDTH, bv);
        Object nlv = new LengthValue(true);
        valueConvertor.put(CSS.Attribute.TEXT_INDENT, nlv);
        valueConvertor.put(CSS.Attribute.WIDTH, lv);
        valueConvertor.put(CSS.Attribute.HEIGHT, lv);
        valueConvertor.put(CSS.Attribute.BORDER_SPACING, lv);
        Object sv = new StringValue();
        valueConvertor.put(CSS.Attribute.FONT_STYLE, sv);
        valueConvertor.put(CSS.Attribute.TEXT_DECORATION, sv);
        valueConvertor.put(CSS.Attribute.TEXT_ALIGN, sv);
        valueConvertor.put(CSS.Attribute.VERTICAL_ALIGN, sv);
        Object valueMapper = new CssValueMapper();
        valueConvertor.put(CSS.Attribute.LIST_STYLE_TYPE,
                           valueMapper);
        valueConvertor.put(CSS.Attribute.BACKGROUND_IMAGE,
                           new BackgroundImage());
        valueConvertor.put(CSS.Attribute.BACKGROUND_POSITION,
                           new BackgroundPosition());
        valueConvertor.put(CSS.Attribute.BACKGROUND_REPEAT,
                           valueMapper);
        valueConvertor.put(CSS.Attribute.BACKGROUND_ATTACHMENT,
                           valueMapper);
        Object generic = new CssValue();
        int n = CSS.Attribute.allAttributes.length;
        for (int i = 0; i < n; i++) {
            CSS.Attribute key = CSS.Attribute.allAttributes[i];
            if (valueConvertor.get(key) == null) {
                valueConvertor.put(key, generic);
            }
        }
    }

    /**
     * Sets the base font size. {@code sz} is a CSS value, and is
     * not necessarily the point size. Use getPointSize to determine the
     * point size corresponding to {@code sz}.
     */
    void setBaseFontSize(int sz) {
        if (sz < 1)
          baseFontSize = 0;
        else if (sz > 7)
          baseFontSize = 7;
        else
          baseFontSize = sz;
    }

    /**
     * Sets the base font size from the passed in string.
     */
    void setBaseFontSize(String size) {
        int relSize, absSize, diff;

        if (size != null) {
            if (size.startsWith("+")) {
                relSize = Integer.parseInt(size.substring(1));
                setBaseFontSize(baseFontSize + relSize);
            } else if (size.startsWith("-")) {
                relSize = -Integer.parseInt(size.substring(1));
                setBaseFontSize(baseFontSize + relSize);
            } else {
                setBaseFontSize(Integer.parseInt(size));
            }
        }
    }

    /**
     * Returns the base font size.
     */
    int getBaseFontSize() {
        return baseFontSize;
    }

    /**
     * Parses the CSS property {@code key} with value
     * {@code value} placing the result in {@code attr}.
     */
    void addInternalCSSValue(MutableAttributeSet attr,
                             CSS.Attribute key, String value) {
        if (key == CSS.Attribute.FONT) {
            ShorthandFontParser.parseShorthandFont(this, value, attr);
        }
        else if (key == CSS.Attribute.BACKGROUND) {
            ShorthandBackgroundParser.parseShorthandBackground
                               (this, value, attr);
        }
        else if (key == CSS.Attribute.MARGIN) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_MARGINS);
        }
        else if (key == CSS.Attribute.PADDING) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_PADDING);
        }
        else if (key == CSS.Attribute.BORDER_WIDTH) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                           CSS.Attribute.ALL_BORDER_WIDTHS);
        }
        else if (key == CSS.Attribute.BORDER_COLOR) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                            CSS.Attribute.ALL_BORDER_COLORS);
        }
        else if (key == CSS.Attribute.BORDER_STYLE) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr,
                                            CSS.Attribute.ALL_BORDER_STYLES);
        }
        else if ((key == CSS.Attribute.BORDER) ||
                   (key == CSS.Attribute.BORDER_TOP) ||
                   (key == CSS.Attribute.BORDER_RIGHT) ||
                   (key == CSS.Attribute.BORDER_BOTTOM) ||
                   (key == CSS.Attribute.BORDER_LEFT)) {
            ShorthandBorderParser.parseShorthandBorder(attr, key, value);
        }
        else {
            Object iValue = getInternalCSSValue(key, value);
            if (iValue != null) {
                attr.addAttribute(key, iValue);
            }
        }
    }

    /**
     * Gets the internal CSS representation of {@code value} which is
     * a CSS value of the CSS attribute named {@code key}. The receiver
     * should not modify {@code value}, and the first {@code count}
     * strings are valid.
     */
    Object getInternalCSSValue(CSS.Attribute key, String value) {
        CssValue conv = (CssValue) valueConvertor.get(key);
        Object r = conv.parseCssValue(value);
        return r != null ? r : conv.parseCssValue(key.getDefaultValue());
    }

    static Object mergeTextDecoration(String value) {
        if (value.startsWith("none")) {
            return null;
        }

        boolean underline = value.contains("underline");
        boolean strikeThrough = value.contains("line-through");
        if (!underline && !strikeThrough) {
            return null;
        }
        String newValue = underline && strikeThrough
                          ? "underline,line-through"
                          : (underline ? "underline" : "line-through");
        return new StringValue().parseCssValue(newValue);
    }

    /**
     * Maps from a StyleConstants to a CSS Attribute.
     */
    Attribute styleConstantsKeyToCSSKey(StyleConstants sc) {
        return styleConstantToCssMap.get(sc);
    }

    /**
     * Maps from a StyleConstants value to a CSS value.
     */
    Object styleConstantsValueToCSSValue(StyleConstants sc,
                                         Object styleValue) {
        Attribute cssKey = styleConstantsKeyToCSSKey(sc);
        if (cssKey != null) {
            CssValue conv = (CssValue)valueConvertor.get(cssKey);
            return conv.fromStyleConstants(sc, styleValue);
        }
        return null;
    }

    /**
     * Converts the passed in CSS value to a StyleConstants value.
     * {@code key} identifies the CSS attribute being mapped.
     */
    Object cssValueToStyleConstantsValue(StyleConstants key, Object value) {
        if (value instanceof CssValue) {
            return ((CssValue)value).toStyleConstants(key, null);
        }
        return null;
    }

    /**
     * Returns the font for the values in the passed in AttributeSet.
     * It is assumed the keys will be CSS.Attribute keys.
     * {@code sc} is the StyleContext that will be messaged to get
     * the font once the size, name and style have been determined.
     */
    Font getFont(StyleContext sc, AttributeSet a, int defaultSize, StyleSheet ss) {
        ss = getStyleSheet(ss);
        int size = getFontSize(a, defaultSize, ss);

        /*
         * If the vertical alignment is set to either superscript or
         * subscript we reduce the font size by 2 points.
         */
        StringValue vAlignV = (StringValue)a.getAttribute
                              (CSS.Attribute.VERTICAL_ALIGN);
        if ((vAlignV != null)) {
            String vAlign = vAlignV.toString();
            if ((vAlign.contains("sup")) ||
                (vAlign.contains("sub"))) {
                size -= 2;
            }
        }

        FontFamily familyValue = (FontFamily)a.getAttribute
                                            (CSS.Attribute.FONT_FAMILY);
        String family = (familyValue != null) ? familyValue.getValue() :
                                  Font.SANS_SERIF;
        int style = Font.PLAIN;
        FontWeight weightValue = (FontWeight) a.getAttribute
                                  (CSS.Attribute.FONT_WEIGHT);
        if ((weightValue != null) && (weightValue.getValue() > 400)) {
            style |= Font.BOLD;
        }
        Object fs = a.getAttribute(CSS.Attribute.FONT_STYLE);
        if ((fs != null) && (fs.toString().contains("italic"))) {
            style |= Font.ITALIC;
        }
        if (family.equalsIgnoreCase("monospace")) {
            family = Font.MONOSPACED;
        }
        Font f = sc.getFont(family, style, size);
        if (f == null
            || (f.getFamily().equals(Font.DIALOG)
                && ! family.equalsIgnoreCase(Font.DIALOG))) {
            family = Font.SANS_SERIF;
            f = sc.getFont(family, style, size);
        }
        return f;
    }

    static int getFontSize(AttributeSet attr, int defaultSize, StyleSheet ss) {
        // PENDING(prinz) this is a 1.1 based implementation, need to also
        // have a 1.2 version.
        FontSize sizeValue = (FontSize)attr.getAttribute(CSS.Attribute.
                                                         FONT_SIZE);

        return (sizeValue != null) ? sizeValue.getValue(attr, ss)
                                   : defaultSize;
    }

    /**
     * Takes a set of attributes and turn it into a color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     * This will return null if there is no value for {@code key}.
     *
     * @param key CSS.Attribute identifying where color is stored.
     * @param a the set of attributes
     * @return the color
     */
    Color getColor(AttributeSet a, CSS.Attribute key) {
        ColorValue cv = (ColorValue) a.getAttribute(key);
        if (cv != null) {
            return cv.getValue();
        }
        return null;
    }

    /**
     * Returns the size of a font from the passed in string.
     *
     * @param size CSS string describing font size
     */
    float getPointSize(String size, StyleSheet ss) {
        int relSize, absSize, diff, index;
        ss = getStyleSheet(ss);
        if (size != null) {
            if (size.startsWith("+")) {
                relSize = Integer.parseInt(size.substring(1));
                return getPointSize(baseFontSize + relSize, ss);
            } else if (size.startsWith("-")) {
                relSize = -Integer.parseInt(size.substring(1));
                return getPointSize(baseFontSize + relSize, ss);
            } else {
                absSize = Integer.parseInt(size);
                return getPointSize(absSize, ss);
            }
        }
        return 0;
    }

    /**
     * Returns the length of the attribute in {@code a} with
     * key {@code key}.
     */
    float getLength(AttributeSet a, CSS.Attribute key, StyleSheet ss) {
        ss = getStyleSheet(ss);
        LengthValue lv = (LengthValue) a.getAttribute(key);
        boolean isW3CLengthUnits = (ss == null) ? false : ss.isW3CLengthUnits();
        float len = (lv != null) ? lv.getValue(isW3CLengthUnits) : 0;
        return len;
    }

    /**
     * Convert a set of HTML attributes to an equivalent
     * set of CSS attributes.
     *
     * @param htmlAttrSet AttributeSet containing the HTML attributes.
     * @return AttributeSet containing the corresponding CSS attributes.
     *        The AttributeSet will be empty if there are no mapping
     *        CSS attributes.
     */
    AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
        MutableAttributeSet cssAttrSet = new SimpleAttributeSet();
        Element elem = (Element)htmlAttrSet;
        HTML.Tag tag = getHTMLTag(htmlAttrSet);
        if ((tag == HTML.Tag.TD) || (tag == HTML.Tag.TH)) {
            // translate border width into the cells, if it has non-zero value.
            AttributeSet tableAttr = elem.getParentElement().
                                     getParentElement().getAttributes();

            int borderWidth = getTableBorder(tableAttr);
            if (borderWidth > 0) {
                // If table contains the BORDER attribute cells should have border width equals 1
                translateAttribute(HTML.Attribute.BORDER, "1", cssAttrSet);
            }
            String pad = (String)tableAttr.getAttribute(HTML.Attribute.CELLPADDING);
            if (pad != null) {
                LengthValue v =
                    (LengthValue)getInternalCSSValue(CSS.Attribute.PADDING_TOP, pad);
                v.span = (v.span < 0) ? 0 : v.span;
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_TOP, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_BOTTOM, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_LEFT, v);
                cssAttrSet.addAttribute(CSS.Attribute.PADDING_RIGHT, v);
            }
        }
        if (elem.isLeaf()) {
            translateEmbeddedAttributes(htmlAttrSet, cssAttrSet);
        } else {
            translateAttributes(tag, htmlAttrSet, cssAttrSet);
        }
        if (tag == HTML.Tag.CAPTION) {
            /*
             * Navigator uses ALIGN for caption placement and IE uses VALIGN.
             */
            Object v = htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
            if ((v != null) && (v.equals("top") || v.equals("bottom"))) {
                cssAttrSet.addAttribute(CSS.Attribute.CAPTION_SIDE, v);
                cssAttrSet.removeAttribute(CSS.Attribute.TEXT_ALIGN);
            } else {
                v = htmlAttrSet.getAttribute(HTML.Attribute.VALIGN);
                if (v != null) {
                    cssAttrSet.addAttribute(CSS.Attribute.CAPTION_SIDE, v);
                }
            }
        }
        return cssAttrSet;
    }

    private static int getTableBorder(AttributeSet tableAttr) {
        String borderValue = (String) tableAttr.getAttribute(HTML.Attribute.BORDER);

        if (borderValue == HTML.NULL_ATTRIBUTE_VALUE || "".equals(borderValue)) {
            // Some browsers accept <TABLE BORDER> and <TABLE BORDER=""> with the same semantics as BORDER=1
            return 1;
        }

        try {
            return Integer.parseInt(borderValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static final Hashtable<String, Attribute> attributeMap = new Hashtable<String, Attribute>();
    private static final Hashtable<String, Value> valueMap = new Hashtable<String, Value>();

    /**
     * The hashtable and the static initialization block below,
     * set up a mapping from well-known HTML attributes to
     * CSS attributes.  For the most part, there is a 1-1 mapping
     * between the two.  However in the case of certain HTML
     * attributes for example HTML.Attribute.VSPACE or
     * HTML.Attribute.HSPACE, end up mapping to two CSS.Attribute's.
     * Therefore, the value associated with each HTML.Attribute.
     * key ends up being an array of CSS.Attribute.* objects.
     */
    private static final Map<HTML.Attribute, CSS.Attribute[]> htmlAttrToCssAttrMap;

    /**
     * The hashtable and static initialization that follows sets
     * up a translation from StyleConstants (i.e. the <em>well known</em>
     * attributes) to the associated CSS attributes.
     */
    private static final Hashtable<Object, Attribute> styleConstantToCssMap = new Hashtable<Object, Attribute>(17);
    /** Maps from HTML value to a CSS value. Used in internal mapping. */
    private static final Hashtable<String, CSS.Value> htmlValueToCssValueMap = new Hashtable<String, CSS.Value>(8);
    /** Maps from CSS value (string) to internal value. */
    private static final Hashtable<String, CSS.Value> cssValueToInternalValueMap = new Hashtable<String, CSS.Value>(13);

    static {
        // load the attribute map
        for (int i = 0; i < Attribute.allAttributes.length; i++ ) {
            attributeMap.put(Attribute.allAttributes[i].toString(),
                             Attribute.allAttributes[i]);
        }
        // load the value map
        for (int i = 0; i < Value.allValues.length; i++ ) {
            valueMap.put(Value.allValues[i].toString(),
                             Value.allValues[i]);
        }
        htmlAttrToCssAttrMap = Map.ofEntries(
                Map.entry(HTML.Attribute.COLOR,
                          new CSS.Attribute[]{CSS.Attribute.COLOR}),
                Map.entry(HTML.Attribute.TEXT,
                          new CSS.Attribute[]{CSS.Attribute.COLOR}),
                Map.entry(HTML.Attribute.CLEAR,
                          new CSS.Attribute[]{CSS.Attribute.CLEAR}),
                Map.entry(HTML.Attribute.BACKGROUND,
                          new CSS.Attribute[]{CSS.Attribute.BACKGROUND_IMAGE}),
                Map.entry(HTML.Attribute.BGCOLOR,
                          new CSS.Attribute[]{CSS.Attribute.BACKGROUND_COLOR}),
                Map.entry(HTML.Attribute.WIDTH,
                          new CSS.Attribute[]{CSS.Attribute.WIDTH}),
                Map.entry(HTML.Attribute.HEIGHT,
                          new CSS.Attribute[]{CSS.Attribute.HEIGHT}),
                Map.entry(HTML.Attribute.BORDER,
                          new CSS.Attribute[]{CSS.Attribute.BORDER_TOP_WIDTH,
                                              CSS.Attribute.BORDER_RIGHT_WIDTH,
                                              CSS.Attribute.BORDER_BOTTOM_WIDTH,
                                              CSS.Attribute.BORDER_LEFT_WIDTH}),
                Map.entry(HTML.Attribute.CELLPADDING,
                          new CSS.Attribute[]{CSS.Attribute.PADDING}),
                Map.entry(HTML.Attribute.CELLSPACING,
                          new CSS.Attribute[]{CSS.Attribute.BORDER_SPACING}),
                Map.entry(HTML.Attribute.MARGINWIDTH,
                          new CSS.Attribute[]{CSS.Attribute.MARGIN_LEFT,
                                              CSS.Attribute.MARGIN_RIGHT}),
                Map.entry(HTML.Attribute.MARGINHEIGHT,
                          new CSS.Attribute[]{CSS.Attribute.MARGIN_TOP,
                                              CSS.Attribute.MARGIN_BOTTOM}),
                Map.entry(HTML.Attribute.HSPACE,
                          new CSS.Attribute[]{CSS.Attribute.PADDING_LEFT,
                                              CSS.Attribute.PADDING_RIGHT}),
                Map.entry(HTML.Attribute.VSPACE,
                          new CSS.Attribute[]{CSS.Attribute.PADDING_BOTTOM,
                                              CSS.Attribute.PADDING_TOP}),
                Map.entry(HTML.Attribute.FACE,
                          new CSS.Attribute[]{CSS.Attribute.FONT_FAMILY}),
                Map.entry(HTML.Attribute.SIZE,
                          new CSS.Attribute[]{CSS.Attribute.FONT_SIZE}),
                Map.entry(HTML.Attribute.VALIGN,
                          new CSS.Attribute[]{CSS.Attribute.VERTICAL_ALIGN}),
                Map.entry(HTML.Attribute.ALIGN,
                          new CSS.Attribute[]{CSS.Attribute.VERTICAL_ALIGN,
                                              CSS.Attribute.TEXT_ALIGN,
                                              CSS.Attribute.FLOAT}),
                Map.entry(HTML.Attribute.TYPE,
                          new CSS.Attribute[]{CSS.Attribute.LIST_STYLE_TYPE}),
                Map.entry(HTML.Attribute.NOWRAP,
                          new CSS.Attribute[]{CSS.Attribute.WHITE_SPACE})
        );

        // initialize StyleConstants mapping
        styleConstantToCssMap.put(StyleConstants.FontFamily,
                                  CSS.Attribute.FONT_FAMILY);
        styleConstantToCssMap.put(StyleConstants.FontSize,
                                  CSS.Attribute.FONT_SIZE);
        styleConstantToCssMap.put(StyleConstants.Bold,
                                  CSS.Attribute.FONT_WEIGHT);
        styleConstantToCssMap.put(StyleConstants.Italic,
                                  CSS.Attribute.FONT_STYLE);
        styleConstantToCssMap.put(StyleConstants.Underline,
                                  CSS.Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.StrikeThrough,
                                  CSS.Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.Superscript,
                                  CSS.Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Subscript,
                                  CSS.Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Foreground,
                                  CSS.Attribute.COLOR);
        styleConstantToCssMap.put(StyleConstants.Background,
                                  CSS.Attribute.BACKGROUND_COLOR);
        styleConstantToCssMap.put(StyleConstants.FirstLineIndent,
                                  CSS.Attribute.TEXT_INDENT);
        styleConstantToCssMap.put(StyleConstants.LeftIndent,
                                  CSS.Attribute.MARGIN_LEFT);
        styleConstantToCssMap.put(StyleConstants.RightIndent,
                                  CSS.Attribute.MARGIN_RIGHT);
        styleConstantToCssMap.put(StyleConstants.SpaceAbove,
                                  CSS.Attribute.MARGIN_TOP);
        styleConstantToCssMap.put(StyleConstants.SpaceBelow,
                                  CSS.Attribute.MARGIN_BOTTOM);
        styleConstantToCssMap.put(StyleConstants.Alignment,
                                  CSS.Attribute.TEXT_ALIGN);

        // HTML->CSS
        htmlValueToCssValueMap.put("disc", CSS.Value.DISC);
        htmlValueToCssValueMap.put("square", CSS.Value.SQUARE);
        htmlValueToCssValueMap.put("circle", CSS.Value.CIRCLE);
        htmlValueToCssValueMap.put("1", CSS.Value.DECIMAL);
        htmlValueToCssValueMap.put("a", CSS.Value.LOWER_ALPHA);
        htmlValueToCssValueMap.put("A", CSS.Value.UPPER_ALPHA);
        htmlValueToCssValueMap.put("i", CSS.Value.LOWER_ROMAN);
        htmlValueToCssValueMap.put("I", CSS.Value.UPPER_ROMAN);

        // CSS-> internal CSS
        cssValueToInternalValueMap.put("none", CSS.Value.NONE);
        cssValueToInternalValueMap.put("disc", CSS.Value.DISC);
        cssValueToInternalValueMap.put("square", CSS.Value.SQUARE);
        cssValueToInternalValueMap.put("circle", CSS.Value.CIRCLE);
        cssValueToInternalValueMap.put("decimal", CSS.Value.DECIMAL);
        cssValueToInternalValueMap.put("lower-roman", CSS.Value.LOWER_ROMAN);
        cssValueToInternalValueMap.put("upper-roman", CSS.Value.UPPER_ROMAN);
        cssValueToInternalValueMap.put("lower-alpha", CSS.Value.LOWER_ALPHA);
        cssValueToInternalValueMap.put("upper-alpha", CSS.Value.UPPER_ALPHA);
        cssValueToInternalValueMap.put("repeat", CSS.Value.BACKGROUND_REPEAT);
        cssValueToInternalValueMap.put("no-repeat",
                                       CSS.Value.BACKGROUND_NO_REPEAT);
        cssValueToInternalValueMap.put("repeat-x",
                                       CSS.Value.BACKGROUND_REPEAT_X);
        cssValueToInternalValueMap.put("repeat-y",
                                       CSS.Value.BACKGROUND_REPEAT_Y);
        cssValueToInternalValueMap.put("scroll",
                                       CSS.Value.BACKGROUND_SCROLL);
        cssValueToInternalValueMap.put("fixed",
                                       CSS.Value.BACKGROUND_FIXED);

        // Register all the CSS attribute keys for archival/unarchival
        Object[] keys = CSS.Attribute.allAttributes;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Register all the CSS Values for archival/unarchival
        keys = CSS.Value.allValues;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the set of all possible CSS attribute keys.
     *
     * @return the set of all possible CSS attribute keys
     */
    public static Attribute[] getAllAttributeKeys() {
        Attribute[] keys = new Attribute[Attribute.allAttributes.length];
        System.arraycopy(Attribute.allAttributes, 0, keys, 0, Attribute.allAttributes.length);
        return keys;
    }

    /**
     * Translates a string to a {@code CSS.Attribute} object.
     * This will return {@code null} if there is no attribute
     * by the given name.
     *
     * @param name the name of the CSS attribute to fetch the
     *  typesafe enumeration for
     * @return the {@code CSS.Attribute} object,
     *  or {@code null} if the string
     *  doesn't represent a valid attribute key
     */
    public static final Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    /**
     * Translates a string to a {@code CSS.Value} object.
     * This will return {@code null} if there is no value
     * by the given name.
     *
     * @param name the name of the CSS value to fetch the
     *  typesafe enumeration for
     * @return the {@code CSS.Value} object,
     *  or {@code null} if the string
     *  doesn't represent a valid CSS value name; this does
     *  not mean that it doesn't represent a valid CSS value
     */
    static final Value getValue(String name) {
        return valueMap.get(name);
    }


    //
    // Conversion related methods/classes
    //

    /**
     * Returns a URL for the given CSS url string. If relative,
     * {@code base} is used as the parent. If a valid URL can not
     * be found, this will not throw a MalformedURLException, instead
     * null will be returned.
     */
    static URL getURL(URL base, String cssString) {
        if (cssString == null) {
            return null;
        }
        if (cssString.startsWith("url(") &&
            cssString.endsWith(")")) {
            cssString = cssString.substring(4, cssString.length() - 1);
        }
        // Absolute first
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL(cssString);
            if (url != null) {
                return url;
            }
        } catch (MalformedURLException mue) {
        }
        // Then relative
        if (base != null) {
            // Relative URL, try from base
            try {
                @SuppressWarnings("deprecation")
                URL url = new URL(base, cssString);
                return url;
            }
            catch (MalformedURLException muee) {
            }
        }
        return null;
    }

    /**
     * Converts a type Color to a hex string
     * in the format "#RRGGBB"
     */
    static String colorToHex(Color color) {

      String colorstr = "#";

      // Red
      String str = Integer.toHexString(color.getRed());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;

      // Green
      str = Integer.toHexString(color.getGreen());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;

      // Blue
      str = Integer.toHexString(color.getBlue());
      if (str.length() > 2)
        str = str.substring(0, 2);
      else if (str.length() < 2)
        colorstr += "0" + str;
      else
        colorstr += str;

      return colorstr;
    }

    /**
     * Convert a "#FFF", "#FFFF", "#FFFFFF" or "#FFFFFFFF" hex string to a Color.
     * If the color specification is bad, an attempt will be made to fix it up.
     */
    static final Color hexToColor(String digits) {
        // CSS Color level 4 allows webpage passes 3, 4, 6 or 8 digit color codes.
        //   - 3 digits #[R][G][B] ........ represents #[RR][GG][BB]FF
        //   - 4 digits #[R][G][B][A] ..... represents #[RR][GG][BB][AA]
        //   - 6 digits #[RR][GG][BB] ..... represents #[RR][GG][BB]FF
        //   - 8 digits #[RR][GG][BB][AA] . represents #[RR][GG][BB][AA]
        final byte[] iseq = digits.startsWith("#") ?
                                 iseqmap.get(Integer.valueOf(digits.length())):
                                 iseqmap.get(Integer.valueOf(-digits.length()));
        if (iseq == null) {
            // Rejects string argument with a wrong number length.
            return null;
        }
        // Only 3, 4, 6 and 8 digits notations.
        // Parses the string argument and build color value.
        int dv;
        int value = 0;
        for (byte i : iseq) {
            dv = i == -15 ? 15 : Character.digit(digits.charAt(i), 16);
            if (dv < 0) {
                // Rejects string argument with at least a non digit Character.
                return null;
            }
            value = dv | value << 4;
        }
        return new Color(value, true);
    }

    // Map of Index Sequences. Index -15 means, use the default value 15.
    private static final Map<Integer, byte[]> iseqmap =
        Map.ofEntries(
            // Positive key, for # prefixed string, is associated with index from 1 to 8.
            // Negative key, for not # prefixed string, is associated with index from 0 to 7.
            Map.entry(Integer.valueOf(4), new byte[]{-15, -15, 1, 1, 2, 2, 3, 3}),
            Map.entry(Integer.valueOf(5), new byte[]{4, 4, 1, 1, 2, 2, 3, 3}),
            Map.entry(Integer.valueOf(7), new byte[]{-15, -15, 1, 2, 3, 4, 5, 6}),
            Map.entry(Integer.valueOf(9), new byte[]{7, 8, 1, 2, 3, 4, 5, 6}),
            Map.entry(Integer.valueOf(-3), new byte[]{-15, -15, 0, 0, 1, 1, 2, 2}),
            Map.entry(Integer.valueOf(-4), new byte[]{3, 3, 0, 0, 1, 1, 2, 2}),
            Map.entry(Integer.valueOf(-6), new byte[]{-15, -15, 0, 1, 2, 3, 4, 5}),
            Map.entry(Integer.valueOf(-8), new byte[]{6, 7, 0, 1, 2, 3, 4, 5})
        );

    private static Map<String, Color> colorNames;

    /**
     * Convert a color string such as "RED" or "#NNNNNN" or "rgb(r, g, b)"
     * or "rgba(r, g, b, a)" to a Color.
     */
    static Color stringToColor(String str) {
        if (str == null) {
            return null;
        } else if (str.isEmpty()) {
            return Color.black;
        }
        String strlc = str.toLowerCase(Locale.ROOT);
        if (strlc.startsWith("rgb(")) {
            return parseRGB(str, (byte)3);
        } else if (strlc.startsWith("rgba(")) {
            return parseRGB(str, (byte)4);
        } else if (strlc.charAt(0) == '#') {
            return hexToColor(str);
        } else {
            if (colorNames == null) {
                colorNames = initColorNames();
            }
            Color color = colorNames.get(strlc);
            if (color != null) {
                return color;
            }
            // sometimes get specified without leading #
            return hexToColor(str);
        }
    }

    private static Map<String, Color> initColorNames() {
        return Map.ofEntries(
            Map.entry("aliceblue", new Color(240, 248, 255)),
            Map.entry("antiquewhite", new Color(250, 235, 215)),
            Map.entry("aqua", new Color(0, 255, 255)),
            Map.entry("aquamarine", new Color(127, 255, 212)),
            Map.entry("azure", new Color(240, 255, 255)),
            Map.entry("beige", new Color(245, 245, 220)),
            Map.entry("bisque", new Color(255, 228, 196)),
            Map.entry("black", new Color(0, 0, 0)),
            Map.entry("blanchedalmond", new Color(255, 235, 205)),
            Map.entry("blue", new Color(0, 0, 255)),
            Map.entry("blueviolet", new Color(138, 43, 226)),
            Map.entry("brown", new Color(165, 42, 42)),
            Map.entry("burlywood", new Color(222, 184, 135)),
            Map.entry("cadetblue", new Color(95, 158, 160)),
            Map.entry("chartreuse", new Color(127, 255, 0)),
            Map.entry("chocolate", new Color(210, 105, 30)),
            Map.entry("coral", new Color(255, 127, 80)),
            Map.entry("cornflowerblue", new Color(100, 149, 237)),
            Map.entry("cornsilk", new Color(255, 248, 220)),
            Map.entry("crimson", new Color(220, 20, 60)),
            Map.entry("cyan", new Color(0, 255, 255)),
            Map.entry("darkblue", new Color(0, 0, 139)),
            Map.entry("darkcyan", new Color(0, 139, 139)),
            Map.entry("darkgoldenrod", new Color(184, 134, 11)),
            Map.entry("darkgray", new Color(169, 169, 169)),
            Map.entry("darkgreen", new Color(0, 100, 0)),
            Map.entry("darkgrey", new Color(169, 169, 169)),
            Map.entry("darkkhaki", new Color(189, 183, 107)),
            Map.entry("darkmagenta", new Color(139, 0, 139)),
            Map.entry("darkolivegreen", new Color(85, 107, 47)),
            Map.entry("darkorange", new Color(255, 140, 0)),
            Map.entry("darkorchid", new Color(153, 50, 204)),
            Map.entry("darkred", new Color(139, 0, 0)),
            Map.entry("darksalmon", new Color(233, 150, 122)),
            Map.entry("darkseagreen", new Color(143, 188, 143)),
            Map.entry("darkslateblue", new Color(72, 61, 139)),
            Map.entry("darkslategray", new Color(47, 79, 79)),
            Map.entry("darkslategrey", new Color(47, 79, 79)),
            Map.entry("darkturquoise", new Color(0, 206, 209)),
            Map.entry("darkviolet", new Color(148, 0, 211)),
            Map.entry("deeppink", new Color(255, 20, 147)),
            Map.entry("deepskyblue", new Color(0, 191, 255)),
            Map.entry("dimgray", new Color(105, 105, 105)),
            Map.entry("dimgrey", new Color(105, 105, 105)),
            Map.entry("dodgerblue", new Color(30, 144, 255)),
            Map.entry("firebrick", new Color(178, 34, 34)),
            Map.entry("floralwhite", new Color(255, 250, 240)),
            Map.entry("forestgreen", new Color(34, 139, 34)),
            Map.entry("fuchsia", new Color(255, 0, 255)),
            Map.entry("gainsboro", new Color(220, 220, 220)),
            Map.entry("ghostwhite", new Color(248, 248, 255)),
            Map.entry("gold", new Color(255, 215, 0)),
            Map.entry("goldenrod", new Color(218, 165, 32)),
            Map.entry("gray", new Color(128, 128, 128)),
            Map.entry("green", new Color(0, 128, 0)),
            Map.entry("greenyellow", new Color(173, 255, 47)),
            Map.entry("grey", new Color(128, 128, 128)),
            Map.entry("honeydew", new Color(240, 255, 240)),
            Map.entry("hotpink", new Color(255, 105, 180)),
            Map.entry("indianred", new Color(205, 92, 92)),
            Map.entry("indigo", new Color(75, 0, 130)),
            Map.entry("ivory", new Color(255, 255, 240)),
            Map.entry("khaki", new Color(240, 230, 140)),
            Map.entry("lavender", new Color(230, 230, 250)),
            Map.entry("lavenderblush", new Color(255, 240, 245)),
            Map.entry("lawngreen", new Color(124, 252, 0)),
            Map.entry("lemonchiffon", new Color(255, 250, 205)),
            Map.entry("lightblue", new Color(173, 216, 230)),
            Map.entry("lightcoral", new Color(240, 128, 128)),
            Map.entry("lightcyan", new Color(224, 255, 255)),
            Map.entry("lightgoldenrodyellow", new Color(250, 250, 210)),
            Map.entry("lightgray", new Color(211, 211, 211)),
            Map.entry("lightgreen", new Color(144, 238, 144)),
            Map.entry("lightgrey", new Color(211, 211, 211)),
            Map.entry("lightpink", new Color(255, 182, 193)),
            Map.entry("lightsalmon", new Color(255, 160, 122)),
            Map.entry("lightseagreen", new Color(32, 178, 170)),
            Map.entry("lightskyblue", new Color(135, 206, 250)),
            Map.entry("lightslategray", new Color(119, 136, 153)),
            Map.entry("lightslategrey", new Color(119, 136, 153)),
            Map.entry("lightsteelblue", new Color(176, 196, 222)),
            Map.entry("lightyellow", new Color(255, 255, 224)),
            Map.entry("lime", new Color(0, 255, 0)),
            Map.entry("limegreen", new Color(50, 205, 50)),
            Map.entry("linen", new Color(250, 240, 230)),
            Map.entry("magenta", new Color(255, 0, 255)),
            Map.entry("maroon", new Color(128, 0, 0)),
            Map.entry("mediumaquamarine", new Color(102, 205, 170)),
            Map.entry("mediumblue", new Color(0, 0, 205)),
            Map.entry("mediumorchid", new Color(186, 85, 211)),
            Map.entry("mediumpurple", new Color(147, 112, 219)),
            Map.entry("mediumseagreen", new Color(60, 179, 113)),
            Map.entry("mediumslateblue", new Color(123, 104, 238)),
            Map.entry("mediumspringgreen", new Color(0, 250, 154)),
            Map.entry("mediumturquoise", new Color(72, 209, 204)),
            Map.entry("mediumvioletred", new Color(199, 21, 133)),
            Map.entry("midnightblue", new Color(25, 25, 112)),
            Map.entry("mintcream", new Color(245, 255, 250)),
            Map.entry("mistyrose", new Color(255, 228, 225)),
            Map.entry("moccasin", new Color(255, 228, 181)),
            Map.entry("navajowhite", new Color(255, 222, 173)),
            Map.entry("navy", new Color(0, 0, 128)),
            Map.entry("oldlace", new Color(253, 245, 230)),
            Map.entry("olive", new Color(128, 128, 0)),
            Map.entry("olivedrab", new Color(107, 142, 35)),
            Map.entry("orange", new Color(255, 165, 0)),
            Map.entry("orangered", new Color(255, 69, 0)),
            Map.entry("orchid", new Color(218, 112, 214)),
            Map.entry("palegoldenrod", new Color(238, 232, 170)),
            Map.entry("palegreen", new Color(152, 251, 152)),
            Map.entry("paleturquoise", new Color(175, 238, 238)),
            Map.entry("palevioletred", new Color(219, 112, 147)),
            Map.entry("papayawhip", new Color(255, 239, 213)),
            Map.entry("peachpuff", new Color(255, 218, 185)),
            Map.entry("peru", new Color(205, 133, 63)),
            Map.entry("pink", new Color(255, 192, 203)),
            Map.entry("plum", new Color(221, 160, 221)),
            Map.entry("powderblue", new Color(176, 224, 230)),
            Map.entry("purple", new Color(128, 0, 128)),
            Map.entry("rebeccapurple", new Color(102, 51, 153)),
            Map.entry("red", new Color(255, 0, 0)),
            Map.entry("rosybrown", new Color(188, 143, 143)),
            Map.entry("royalblue", new Color(65, 105, 225)),
            Map.entry("saddlebrown", new Color(139, 69, 19)),
            Map.entry("salmon", new Color(250, 128, 114)),
            Map.entry("sandybrown", new Color(244, 164, 96)),
            Map.entry("seagreen", new Color(46, 139, 87)),
            Map.entry("seashell", new Color(255, 245, 238)),
            Map.entry("sienna", new Color(160, 82, 45)),
            Map.entry("silver", new Color(192, 192, 192)),
            Map.entry("skyblue", new Color(135, 206, 235)),
            Map.entry("slateblue", new Color(106, 90, 205)),
            Map.entry("slategray", new Color(112, 128, 144)),
            Map.entry("slategrey", new Color(112, 128, 144)),
            Map.entry("snow", new Color(255, 250, 250)),
            Map.entry("springgreen", new Color(0, 255, 127)),
            Map.entry("steelblue", new Color(70, 130, 180)),
            Map.entry("tan", new Color(210, 180, 140)),
            Map.entry("teal", new Color(0, 128, 128)),
            Map.entry("thistle", new Color(216, 191, 216)),
            Map.entry("tomato", new Color(255, 99, 71)),
            Map.entry("transparent", new Color(0, 0, 0, 0)),
            Map.entry("turquoise", new Color(64, 224, 208)),
            Map.entry("violet", new Color(238, 130, 238)),
            Map.entry("wheat", new Color(245, 222, 179)),
            Map.entry("white", new Color(255, 255, 255)),
            Map.entry("whitesmoke", new Color(245, 245, 245)),
            Map.entry("yellow", new Color(255, 255, 0)),
            Map.entry("yellowgreen", new Color(154, 205, 50))
            );
    }

    /**
     * Parses a String using the grammar described in the
     * <a href="https://www.w3.org/TR/css-color-4/#rgb-functions">
     *     CSS-COLOR-4 5.1.The RGB functions
     * </a> specifications.
     * By example {@code rgb(rc gc bc)}, {@code rgb(rc,gc,bc)},
     * {@code rgb(rc gc bc / ac)}, {@code rgb(rc,gc,bc,ac)} including
     * {@code rgba} notations are valid. Each of the rc, gc, bc color
     * components is a number in [[0,255]] or a percentage (number with a % after),
     * indicating a percentage value of the color channel maximal value.
     * These values are constrained to repectively fit with 0-255 or 0%-100%.
     * The ac Color component is a single number or a percentage (number with
     * a %) in [0,1].
     * This value is constrained to fit with 0-1 or 0%-100%.
     * See
     * <a href="https://www.w3.org/TR/css-values-4/#number-value">
     *     number
     * </a> and
     * <a href="https://www.w3.org/TR/css-values-4/#percentage-value">
     *     percentage
     * </a> in the
     * <a href="https://www.w3.org/TR/css-values-4">
     *    CSS-VALUE-4
     * </a> specifications.
     * The resulting Color is returned.
     */
    private static Color parseRGB(String string, byte start) {

        // The array index argument :
        // - first element : start parsing index.
        // - second element : the color channel,
        //   -
        //   - red   : 1.
        //   - green : 2.
        //   - blue  : 3.
        //   - alpha : 4.
        // - third element : type value.
        //   - indeterminated : -1.
        //   - conflict       : 0. return black color.
        //   - a number       : 1. rc, gc and bc are numbers.
        //         Use Color(int r, int g, int b) or
        //         Color(int r, int g, int b, int a). Linear transform alpha value from [0,1] to [0,255].
        //   - a percentage   : 2. rc, gc and bc are percentages.
        //         Use Color(float r, float g, float b) or
        //         Color(float r, float g, float b, float a).
        byte[] index = new byte[3];

        // Find the next numeric char
        index[0] = start;
        index[1] = 1;
        index[2] = -1;
        // A negative compoment means color is not well formed and
        float red = getColorComponent(string, index);
        if (red < 0) return new Color(0f, 0f, 0f);
        float green = getColorComponent(string, index);
        if (green < 0) return new Color(0f, 0f, 0f);
        float blue = getColorComponent(string, index);
        if (blue < 0) return new Color(0f, 0f, 0f);
        float alpha = getColorComponent(string, index);
        if (alpha == -1) return new Color(0f, 0f, 0f);
        if (alpha < 0) {
            return new Color(red, green, blue);
        } else {
            return new Color(red, green, blue, alpha);
        }
    }

    /*
    private static Color parseRGBA(String string) {
        // Find the next numeric char
        byte[] index = new byte[2];

        index[0] = 4;
        index[1] = 1;
        float red = getColorComponent(string, index);
        if (red < 0) return new Color(0, 0, 0);
        float green = getColorComponent(string, index);
        if (green < 0) return new Color(0, 0, 0);
        float blue = getColorComponent(string, index);
        if (blue < 0) return new Color(0, 0, 0);
        float alpha = getColorComponent(string, index);
        if (alpha == -1) return new Color(0, 0, 0);
        if (alpha < 0) {
            return new Color(red, green, blue);
        } else {
            return new Color(red, green, blue, alpha);
        }
    }
    */

    private static int SIGNIFICAND_MAX = 655350000;
    /**
     * Returns the next float value from {@code string} starting
     * at {@code index[0]}. The value can either can a number, or
     * a percentage (number ending with %), in which case it is
     * multiplied by 255 except for the alpha Color component.
     */
    private static float getColorComponent(String string, byte[] index) {
        int length = string.length();
        char aChar = '?';
        boolean sep = false;
        // Only one separator
        boolean oosep = false;

        // Principles
        // - the attribute value is supposed to be well-formed i.e. as provided by the CSSParser.java :
        //   - Does not contain white space(s) at the beginning of the string and the end of the string.
        //   - Match the sequence directly following the name of the rgb or rgba functions.
        //     White-space(s) between rgb or rgba and ( is (are) removed.
        //   - Multiple spaces between '(', value, ',' or ')' are replaced by only one space.
        //   - contains well formed blocks (), this is ensured by the parser CSSParser.java but through an Exception.
        //   - Components are well-formed and must be a none keyword, a number or a percentage.
        //   - The sequence of characters is predictable.
        // - Any deviation from the expected, generates the rejection of the component.

        // Must return :
        // - a positive float if succeed to parse a decimal value or percent value.
        // - the value -1 if the argument format is wrong.
        // - the value -2 if failed to parse expected separator.
        // - the value -3 if conflict between number and percentage.
        //
        // The index argument
        // - Update the index of
        //   - start parsing index value,
        //   - component,
        //   -
        // - the value -3 rc, gc and bc are numbers. Use Color(int r, int g, int b) or Color(int r, int g, int b, int a).
        // - the value -4 rc, gc and bc are percentages. Use Color(float r, float g, float b) or Color(float r, float g, float b, float a).

        // Alphabet :
        // - "( ,.-+0123456789/%)noe"

        // Keyword :
        // - Only one : none

        // Grammar
        // - argument :== ("rgb" | "rgba") "(" {<decimal-number> "%"? (" "* | ",")}3 ["/" <decimal-number> "%"?] ")"
        // - decimal-number :== ["+" | "-"] digit* ["." digit*]  ["e" ["+" | "-"] digit*]
        // - digit :== "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
        // - rgbc :: 'rgb'
        // - delimitors :: = ( none
        //
        // Skip unauthorized characters :
        // - Starting number alphabet
        //   - non-number sign,
        //   - non-decimal digit,
        // - Separator signs
        //   - non-whitespace,
        //   - non-comma,
        //   - non-slash,
        // And store last word of 6 characters, may contain keyword "none"

        // Try to detect the rigth separator and reach the first character different from ' '.
        String seplist = index[1] == 1 ? "( " :
                         index[1] > 3 ? " ,/" : " ,";

        // Can continue to scan
        boolean notReachEnd = true;
        while (notReachEnd = (index[0] < length
              && ((aChar = string.charAt(index[0])) != ')'))) {
            if (seplist.indexOf(aChar) > -1/* || Character.isSpaceChar(aChar)*/) {
                sep = true;
                if (oosep && aChar != ' ') {
                    // Two consecutive non space separators.
                    // - Never happens with a parsed string because of CSSParser
                    // - But useful for StyleSheet.stringToColor
                    return -1;
                } else {
                    if (aChar != ' ') oosep = true;
                }
            } else {
                break;
            }
            index[0]++;
        }
        // End of string or no separator detected, terminate.
        if (!notReachEnd || !sep) return -2;

        // From here
        // - current char is in +-0123456789n
        // - start build value
        //   - value could be 0 if none detected
        //   - a number or a percentage

        // Try to detect 'none' keyword
        if (aChar == 'n') {
            String snkw = string.substring(index[0], Math.min(index[0] + 4, length));
            if (snkw.equals("none")) {
                index[1]++;
                index[0] += 4;
                return 0;
            } else {
                return -1;
            }
        }

        // From here, next expected char is only +-0123456789
        int start = index[0];
        boolean negative = false;
        if (aChar == '+' || (negative = aChar == '-')) {
            index[0]++;
        }

        // From here next expected chars are only 0123456789
        int significand = 0;
        int tenpower = 0;
        float fvalueIntergerPart = 0;
        float fvalueFractionalPart = 0;

        while (notReachEnd = (index[0] < length
               && ((aChar = string.charAt(index[0])) != ')')
               && Character.isDigit(aChar))) {
            index[0]++;
            if (significand < SIGNIFICAND_MAX) {
                significand = significand * 10 + (aChar - '0')/* * 10000*/;
            } else if (tenpower < Integer.MAX_VALUE) {
                tenpower++;
            } else {
                // Treatement over capacity Treat as 0, not sure ! May as infinite.
                return 0;
            }
        }
        if (index[0] < length && string.charAt(index[0]) == '.') {
            // Fractional part
            index[0]++;
            // From here next expected chars are only 0123456789.
            // Only next four digits are take in count.
            while (index[0] < length
                  && ((aChar = string.charAt(index[0])) != ')')
                  && Character.isDigit(aChar)) {
                index[0]++;
                if (significand < SIGNIFICAND_MAX) {
                   if (tenpower > Integer.MIN_VALUE) {
                       significand = significand * 10 + (aChar - '0');
                       tenpower--;
                   }
                }
            }
        }
        if (index[0] < length && (string.charAt(index[0]) == 'e' || string.charAt(index[0]) == 'E')) {
            boolean negativeExpSign = false;
            index[0]++;
            if (index[0] < length && ((aChar = string.charAt(index[0])) == '+' || (negativeExpSign = (aChar == '-')))) {
                index[0]++;
            }
            // Exponent value
            int exponent = 0;
            // From here next expected chars are only 0123456789
            while (index[0] < length
                  && ((aChar = string.charAt(index[0])) != ')')
                  && Character.isDigit(aChar)) {
                index[0]++;
                exponent = exponent * 10 + aChar - '0';
            }
            tenpower = negativeExpSign ? tenpower - exponent : tenpower + exponent;
        }

        // From here next expected char is "% ,/)"
        if (index[0] < length
            && "% ,/)".indexOf(aChar) < 0) return -1;
        if (tenpower < -10) return 0;
        // Not sure : over capacity should return infinite.
        if (tenpower > 10) return 0;
        if (start != index[0]) {
            float value = significand;
            if (index[0] < length && string.charAt(index[0]) == '%') {
                index[0]++;
                /* Should replace the previous line in the next version
                if (index[2] == 2) {
                    index[0]++;
                } else {
                    index[2] = 0;
                    return 0;
                }
                */
                if (tenpower > Integer.MIN_VALUE + 2) {
                    tenpower -= 2;
                } else return 0;
            } else if (index[1] < 4) {
                value = value / 255f;
                /* Should replace the previous line in the next version
                if (index[2] == 1) {
                    value = value / 255f;
                } else {
                    index[2] = 0;
                    return 0;
                }
                */
            }
            // Reject negative value;
            // Clamp value in [[0,255]] or [0,1]
            // By example -12 ou -12% must return 0 !
            // Not return before to be sure the value is valid.
            // That's why, I set it here.
            if (negative) return 0;
            if (tenpower < 0) {
                value = value / FLOAT_10_POW[-tenpower];
            } else {
                value = value * FLOAT_10_POW[tenpower];
            }
            index[1]++;
            return Math.min(1, value);
        }
        return -1;
    }

    private static final float[] FLOAT_10_POW = {
        1.0e0f,
        1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
        1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    static int getIndexOfSize(float pt, int[] sizeMap) {
        for (int i = 0; i < sizeMap.length; i ++ )
                if (pt <= sizeMap[i])
                        return i + 1;
        return sizeMap.length;
    }

    static int getIndexOfSize(float pt, StyleSheet ss) {
        int[] sizeMap = (ss != null) ? ss.getSizeMap() :
            StyleSheet.sizeMapDefault;
        return getIndexOfSize(pt, sizeMap);
    }


    /**
     * @return an array of all the strings in {@code value}
     *         that are separated by whitespace.
     */
    static String[] parseStrings(String value) {
        int         current, last;
        int         length = (value == null) ? 0 : value.length();
        ArrayList<String> temp = new ArrayList<String>(4);

        current = 0;
        while (current < length) {
            // Skip ws
            while (current < length && Character.isWhitespace
                   (value.charAt(current))) {
                current++;
            }
            last = current;
            int inParentheses = 0;
            char ch;
            while (current < length && (
                    !Character.isWhitespace(ch = value.charAt(current))
                            || inParentheses > 0)) {
                if (ch == '(') {
                    inParentheses++;
                } else if (ch == ')') {
                    inParentheses--;
                }
                current++;
            }
            if (last != current) {
                temp.add(value.substring(last, current));
            }
            current++;
        }
        String[] retValue = temp.toArray(new String[0]);
        return retValue;
    }

    /**
     * Return the point size, given a size index. Legal HTML index sizes
     * are 1-7.
     */
    float getPointSize(int index, StyleSheet ss) {
        ss = getStyleSheet(ss);
        int[] sizeMap = (ss != null) ? ss.getSizeMap() :
            StyleSheet.sizeMapDefault;
        --index;
        if (index < 0)
          return sizeMap[0];
        else if (index > sizeMap.length - 1)
          return sizeMap[sizeMap.length - 1];
        else
          return sizeMap[index];
    }


    private void translateEmbeddedAttributes(AttributeSet htmlAttrSet,
                                             MutableAttributeSet cssAttrSet) {
        Enumeration<?> keys = htmlAttrSet.getAttributeNames();
        if (htmlAttrSet.getAttribute(StyleConstants.NameAttribute) ==
            HTML.Tag.HR) {
            // HR needs special handling due to us treating it as a leaf.
            translateAttributes(HTML.Tag.HR, htmlAttrSet, cssAttrSet);
        }
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)key;
                Object o = htmlAttrSet.getAttribute(tag);
                if (o instanceof AttributeSet as) {
                    translateAttributes(tag, as, cssAttrSet);
                }
            } else if (key instanceof CSS.Attribute) {
                cssAttrSet.addAttribute(key, htmlAttrSet.getAttribute(key));
            }
        }
    }

    private void translateAttributes(HTML.Tag tag,
                                            AttributeSet htmlAttrSet,
                                            MutableAttributeSet cssAttrSet) {
        Enumeration<?> names = htmlAttrSet.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();

            if (name instanceof HTML.Attribute) {
                HTML.Attribute key = (HTML.Attribute)name;

                /*
                 * HTML.Attribute.ALIGN needs special processing.
                 * It can map to 1 of many(3) possible CSS attributes
                 * depending on the nature of the tag the attribute is
                 * part off and depending on the value of the attribute.
                 */
                if (key == HTML.Attribute.ALIGN) {
                    String htmlAttrValue = (String)htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
                    if (htmlAttrValue != null) {
                        CSS.Attribute cssAttr = getCssAlignAttribute(tag, htmlAttrSet);
                        if (cssAttr != null) {
                            Object o = getCssValue(cssAttr, htmlAttrValue);
                            if (o != null) {
                                cssAttrSet.addAttribute(cssAttr, o);
                            }
                        }
                    }
                } else {
                    if (key == HTML.Attribute.SIZE && !isHTMLFontTag(tag)) {
                        /*
                         * The html size attribute has a mapping in the CSS world only
                         * if it is par of a font or base font tag.
                         */
                    } else if (tag == HTML.Tag.TABLE && key == HTML.Attribute.BORDER) {
                        int borderWidth = getTableBorder(htmlAttrSet);

                        if (borderWidth > 0) {
                            translateAttribute(HTML.Attribute.BORDER, Integer.toString(borderWidth), cssAttrSet);
                        }
                    } else {
                        translateAttribute(key, (String) htmlAttrSet.getAttribute(key), cssAttrSet);
                    }
                }
            } else if (name instanceof CSS.Attribute) {
                cssAttrSet.addAttribute(name, htmlAttrSet.getAttribute(name));
            }
        }
    }

    private void translateAttribute(HTML.Attribute key,
                                           String htmlAttrValue,
                                           MutableAttributeSet cssAttrSet) {
        /*
         * In the case of all remaining HTML.Attribute's they
         * map to 1 or more CCS.Attribute.
         */
        CSS.Attribute[] cssAttrList = getCssAttribute(key);

        if (cssAttrList == null || htmlAttrValue == null) {
            return;
        }
        for (Attribute cssAttr : cssAttrList) {
            Object o = getCssValue(cssAttr, htmlAttrValue);
            if (o != null) {
                cssAttrSet.addAttribute(cssAttr , o);
            }
        }
    }

    /**
     * Given a CSS.Attribute object and its corresponding HTML.Attribute's
     * value, this method returns a CssValue object to associate with the
     * CSS attribute.
     *
     * @param cssAttr the CSS.Attribute
     * @param htmlAttrValue a String containing the value associated HTML.Attribute.
     */
    Object getCssValue(CSS.Attribute cssAttr, String htmlAttrValue) {
        CssValue value = (CssValue)valueConvertor.get(cssAttr);
        Object o = value.parseHtmlValue(htmlAttrValue);
        return o;
    }

    /**
     * Maps an HTML.Attribute object to its appropriate CSS.Attributes.
     *
     * @param hAttr HTML.Attribute
     * @return CSS.Attribute[]
     */
    private CSS.Attribute[] getCssAttribute(HTML.Attribute hAttr) {
        return htmlAttrToCssAttrMap.get(hAttr);
    }

    /**
     * Maps HTML.Attribute.ALIGN to either:
     *     CSS.Attribute.TEXT_ALIGN
     *     CSS.Attribute.FLOAT
     *     CSS.Attribute.VERTICAL_ALIGN
     * based on the tag associated with the attribute and the
     * value of the attribute.
     *
     * @param tag the AttributeSet containing HTML attributes.
     * @return CSS.Attribute mapping for HTML.Attribute.ALIGN.
     */
    private CSS.Attribute getCssAlignAttribute(HTML.Tag tag,
                                                   AttributeSet htmlAttrSet) {
        return CSS.Attribute.TEXT_ALIGN;
/*
        String htmlAttrValue = (String)htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
        CSS.Attribute cssAttr = CSS.Attribute.TEXT_ALIGN;
        if (htmlAttrValue != null && htmlAttrSet instanceof Element) {
            Element elem = (Element)htmlAttrSet;
            if (!elem.isLeaf() && tag.isBlock() && validTextAlignValue(htmlAttrValue)) {
                return CSS.Attribute.TEXT_ALIGN;
            } else if (isFloater(htmlAttrValue)) {
                return CSS.Attribute.FLOAT;
            } else if (elem.isLeaf()) {
                return CSS.Attribute.VERTICAL_ALIGN;
            }
        }
        return null;
        */
    }

    /**
     * Fetches the tag associated with the HTML AttributeSet.
     *
     * @param  htmlAttrSet the AttributeSet containing the HTML attributes.
     * @return HTML.Tag
     */
    private HTML.Tag getHTMLTag(AttributeSet htmlAttrSet) {
        Object o = htmlAttrSet.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag tag = (HTML.Tag) o;
            return tag;
        }
        return null;
    }


    private boolean isHTMLFontTag(HTML.Tag tag) {
        return (tag != null && ((tag == HTML.Tag.FONT) || (tag == HTML.Tag.BASEFONT)));
    }


    private boolean isFloater(String alignValue) {
        return (alignValue.equals("left") || alignValue.equals("right"));
    }

    private boolean validTextAlignValue(String alignValue) {
        return (isFloater(alignValue) || alignValue.equals("center"));
    }

    /**
     * Base class to CSS values in the attribute sets.  This
     * is intended to act as a converter to/from other attribute
     * formats.
     * <p>
     * The CSS parser uses the parseCssValue method to convert
     * a string to whatever format is appropriate for a given key
     * (i.e. these converters are stored in a map using the
     * CSS.Attribute as the key and the CssValue as the value).
     * <p>
     * The HTML to CSS conversion process first converts the
     * HTML.Attribute to a CSS.Attribute, and then calls
     * the parseHtmlValue method on the value of the HTML
     * attribute to produce the corresponding CSS value.
     * <p>
     * The StyleConstants to CSS conversion process first
     * converts the StyleConstants attribute to a
     * CSS.Attribute, and then calls the fromStyleConstants
     * method to convert the StyleConstants value to a
     * CSS value.
     * <p>
     * The CSS to StyleConstants conversion process first
     * converts the StyleConstants attribute to a
     * CSS.Attribute, and then calls the toStyleConstants
     * method to convert the CSS value to a StyleConstants
     * value.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class CssValue implements Serializable {

        /**
         * Convert a CSS value string to the internal format
         * (for fast processing) used in the attribute sets.
         * The fallback storage for any value that we don't
         * have a special binary format for is a String.
         */
        Object parseCssValue(String value) {
            return value;
        }

        /**
         * Convert an HTML attribute value to a CSS attribute
         * value.  If there is no conversion, return null.
         * This is implemented to simply forward to the CSS
         * parsing by default (since some of the attribute
         * values are the same).  If the attribute value
         * isn't recognized as a CSS value it is generally
         * returned as null.
         */
        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion,
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            return null;
        }

        /**
         * Converts a CSS attribute value to a
         * {@code StyleConstants}
         * value.  If there is no conversion, returns
         * {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param v the view containing {@code AttributeSet}
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            return null;
        }

        /**
         * Return the CSS format of the value
         */
        public String toString() {
            return svalue;
        }

        /**
         * The value as a string... before conversion to a
         * binary format.
         */
        String svalue;
    }

    /**
     * By default CSS attributes are represented as simple
     * strings.  They also have no conversion to/from
     * StyleConstants by default. This class represents the
     * value as a string (via the superclass), but
     * provides StyleConstants conversion support for the
     * CSS attributes that are held as strings.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class StringValue extends CssValue {

        /**
         * Convert a CSS value string to the internal format
         * (for fast processing) used in the attribute sets.
         * This produces a StringValue, so that it can be
         * used to convert from CSS to StyleConstants values.
         */
        Object parseCssValue(String value) {
            StringValue sv = new StringValue();
            sv.svalue = value;
            return sv;
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion
         * returns {@code null}.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (key == StyleConstants.Italic) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("italic");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Underline) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("underline");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Alignment) {
                int align = ((Integer)value).intValue();
                String ta;
                switch(align) {
                case StyleConstants.ALIGN_LEFT:
                    ta = "left";
                    break;
                case StyleConstants.ALIGN_RIGHT:
                    ta = "right";
                    break;
                case StyleConstants.ALIGN_CENTER:
                    ta = "center";
                    break;
                case StyleConstants.ALIGN_JUSTIFIED:
                    ta = "justify";
                    break;
                default:
                    ta = "left";
                }
                return parseCssValue(ta);
            } else if (key == StyleConstants.StrikeThrough) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("line-through");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Superscript) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("super");
                }
                return parseCssValue("");
            } else if (key == StyleConstants.Subscript) {
                if (value.equals(Boolean.TRUE)) {
                    return parseCssValue("sub");
                }
                return parseCssValue("");
            }
            return null;
        }

        /**
         * Converts a CSS attribute value to a
         * {@code StyleConstants} value.
         * If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            if (key == StyleConstants.Italic) {
                if (svalue.contains("italic")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Underline) {
                if (svalue.contains("underline")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Alignment) {
                if (svalue.equals("right")) {
                    return StyleConstants.ALIGN_RIGHT;
                } else if (svalue.equals("center")) {
                    return StyleConstants.ALIGN_CENTER;
                } else if  (svalue.equals("justify")) {
                    return StyleConstants.ALIGN_JUSTIFIED;
                }
                return StyleConstants.ALIGN_LEFT;
            } else if (key == StyleConstants.StrikeThrough) {
                if (svalue.contains("line-through")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Superscript) {
                if (svalue.contains("super")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (key == StyleConstants.Subscript) {
                if (svalue.contains("sub")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            return null;
        }

        // Used by ViewAttributeSet
        boolean isItalic() {
            return (svalue.contains("italic"));
        }

        boolean isStrike() {
            return (svalue.contains("line-through"));
        }

        boolean isUnderline() {
            return (svalue.contains("underline"));
        }

        boolean isSub() {
            return (svalue.contains("sub"));
        }

        boolean isSup() {
            return (svalue.contains("sup"));
        }

        @Override
        public int hashCode() {
            return (this.svalue != null) ? this.svalue.hashCode() : 0;
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.StringValue strVal
                   && Objects.equals(this.svalue, strVal.svalue);
        }

    }

    /**
     * Represents a value for the CSS.FONT_SIZE attribute.
     * The binary format of the value can be one of several
     * types.  If the type is Float,
     * the value is specified in terms of point or
     * percentage, depending upon the ending of the
     * associated string.
     * If the type is Integer, the value is specified
     * in terms of a size index.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    class FontSize extends CssValue {

        /**
         * Returns the size in points.  This is ultimately
         * what we need for the purpose of creating/fetching
         * a Font object.
         *
         * @param a the attribute set the value is being
         *  requested from.  We may need to walk up the
         *  resolve hierarchy if it's relative.
         */
        int getValue(AttributeSet a, StyleSheet ss) {
            ss = getStyleSheet(ss);
            if (index) {
                // it's an index, translate from size table
                return Math.round(getPointSize((int) value, ss));
            }
            else if (lu == null) {
                return Math.round(value);
            }
            else {
                if (lu.type == 0) {
                    boolean isW3CLengthUnits = (ss == null) ? false : ss.isW3CLengthUnits();
                    return Math.round(lu.getValue(isW3CLengthUnits));
                }
                if (a != null) {
                    AttributeSet resolveParent = a.getResolveParent();

                    if (resolveParent != null) {
                        int pValue = StyleConstants.getFontSize(resolveParent);

                        float retValue;
                        if (lu.type == 1 || lu.type == 3) {
                            retValue = lu.value * (float)pValue;
                        }
                        else {
                            retValue = lu.value + (float)pValue;
                        }
                        return Math.round(retValue);
                    }
                }
                // a is null, or no resolve parent.
                return 12;
            }
        }

        Object parseCssValue(String value) {
            FontSize fs = new FontSize();
            fs.svalue = value;
            try {
                if (value.equals("xx-small")) {
                    fs.value = 1;
                    fs.index = true;
                } else if (value.equals("x-small")) {
                    fs.value = 2;
                    fs.index = true;
                } else if (value.equals("small")) {
                    fs.value = 3;
                    fs.index = true;
                } else if (value.equals("medium")) {
                    fs.value = 4;
                    fs.index = true;
                } else if (value.equals("large")) {
                    fs.value = 5;
                    fs.index = true;
                } else if (value.equals("x-large")) {
                    fs.value = 6;
                    fs.index = true;
                } else if (value.equals("xx-large")) {
                    fs.value = 7;
                    fs.index = true;
                } else {
                    fs.lu = new LengthUnit(value, (short)1, 1f);
                }
                // relative sizes, larger | smaller (adjust from parent by
                // 1.5 pixels)
                // em, ex refer to parent sizes
                // lengths: pt, mm, cm, pc, in, px
                //          em (font height 3em would be 3 times font height)
                //          ex (height of X)
                // lengths are (+/-) followed by a number and two letter
                // unit identifier
            } catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }

        Object parseHtmlValue(String value) {
            if ((value == null) || (value.length() == 0)) {
                return null;
            }
            FontSize fs = new FontSize();
            fs.svalue = value;

            try {
                /*
                 * relative sizes in the size attribute are relative
                 * to the <basefont>'s size.
                 */
                int baseFontSize = getBaseFontSize();
                if (value.charAt(0) == '+') {
                    int relSize = Integer.parseInt(value.substring(1));
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else if (value.charAt(0) == '-') {
                    int relSize = -Integer.parseInt(value.substring(1));
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else {
                    fs.value = Integer.parseInt(value);
                    if (fs.value > 7) {
                        fs.value = 7;
                    } else if (fs.value < 0) {
                        fs.value = 0;
                    }
                    fs.index = true;
                }

            } catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value instanceof Number) {
                FontSize fs = new FontSize();

                fs.value = getIndexOfSize(((Number)value).floatValue(), StyleSheet.sizeMapDefault);
                fs.svalue = Integer.toString((int)fs.value);
                fs.index = true;
                return fs;
            }
            return parseCssValue(value.toString());
        }

        /**
         * Converts a CSS attribute value to a {@code StyleConstants}
         * value.  If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            if (v != null) {
                return Integer.valueOf(getValue(v.getAttributes(), null));
            }
            return Integer.valueOf(getValue(null, null));
        }

        @Override
        public int hashCode() {
            return Float.hashCode(value)
                   | Boolean.hashCode(index)
                   | Objects.hashCode(lu);
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.FontSize size
                   && value == size.value
                   && index == size.index
                   && Objects.equals(lu, size.lu);
        }

        float value;
        boolean index;
        LengthUnit lu;
    }

    @SuppressWarnings("serial") // Same-version serialization only
    static class FontFamily extends CssValue {

        /**
         * Returns the font family to use.
         */
        String getValue() {
            return family;
        }

        Object parseCssValue(String value) {
            int cIndex = value.indexOf(',');
            FontFamily ff = new FontFamily();
            ff.svalue = value;
            ff.family = null;

            if (cIndex == -1) {
                setFontName(ff, value);
            }
            else {
                boolean done = false;
                int lastIndex;
                int length = value.length();
                cIndex = 0;
                while (!done) {
                    // skip ws.
                    while (cIndex < length &&
                           Character.isWhitespace(value.charAt(cIndex)))
                        cIndex++;
                    // Find next ','
                    lastIndex = cIndex;
                    cIndex = value.indexOf(',', cIndex);
                    if (cIndex == -1) {
                        cIndex = length;
                    }
                    if (lastIndex < length) {
                        if (lastIndex != cIndex) {
                            int lastCharIndex = cIndex;
                            if (cIndex > 0 && value.charAt(cIndex - 1) == ' '){
                                lastCharIndex--;
                            }
                            setFontName(ff, value.substring
                                        (lastIndex, lastCharIndex));
                            done = (ff.family != null);
                        }
                        cIndex++;
                    }
                    else {
                        done = true;
                    }
                }
            }
            if (ff.family == null) {
                ff.family = Font.SANS_SERIF;
            }
            return ff;
        }

        private void setFontName(FontFamily ff, String fontName) {
            ff.family = fontName;
        }

        Object parseHtmlValue(String value) {
            // TBD
            return parseCssValue(value);
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            return parseCssValue(value.toString());
        }

        /**
         * Converts a CSS attribute value to a {@code StyleConstants}
         * value.  If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            return family;
        }

        @Override
        public int hashCode() {
            return (family != null) ? family.hashCode() : 0;
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.FontFamily font
                   && Objects.equals(family, font.family);
        }

        String family;
    }

    @SuppressWarnings("serial") // Same-version serialization only
    static class FontWeight extends CssValue {

        int getValue() {
            return weight;
        }

        Object parseCssValue(String value) {
            FontWeight fw = new FontWeight();
            fw.svalue = value;
            if (value.equals("bold")) {
                fw.weight = 700;
            } else if (value.equals("normal")) {
                fw.weight = 400;
            } else {
                // PENDING(prinz) add support for relative values
                try {
                    fw.weight = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    fw = null;
                }
            }
            return fw;
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value.equals(Boolean.TRUE)) {
                return parseCssValue("bold");
            }
            return parseCssValue("normal");
        }

        /**
         * Converts a CSS attribute value to a {@code StyleConstants}
         * value.  If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            return (weight > 500) ? Boolean.TRUE : Boolean.FALSE;
        }

        boolean isBold() {
            return (weight > 500);
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(weight);
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.FontWeight fontWeight
                   && weight == fontWeight.weight;
        }

        int weight;
    }

    @SuppressWarnings("serial") // Same-version serialization only
    static class ColorValue extends CssValue {

        /**
         * Returns the color to use.
         */
        Color getValue() {
            return c;
        }

        Object parseCssValue(String value) {

            Color c = stringToColor(value);
            if (c != null) {
                ColorValue cv = new ColorValue();
                cv.svalue = value;
                cv.c = c;
                return cv;
            }
            return null;
        }

        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }

        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            ColorValue colorValue = new ColorValue();
            colorValue.c = (Color)value;
            colorValue.svalue = colorToHex(colorValue.c);
            return colorValue;
        }

        /**
         * Converts a CSS attribute value to a {@code StyleConstants}
         * value.  If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            return c;
        }

        @Override
        public int hashCode() {
            return (c != null) ? c.hashCode() : 0;
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.ColorValue color && c.equals(color.c);
        }

        Color c;
    }

    @SuppressWarnings("serial") // Same-version serialization only
    static class BorderStyle extends CssValue {

        CSS.Value getValue() {
            return style;
        }

        Object parseCssValue(String value) {
            CSS.Value cssv = CSS.getValue(value);
            if (cssv != null) {
                if ((cssv == CSS.Value.INSET) ||
                    (cssv == CSS.Value.OUTSET) ||
                    (cssv == CSS.Value.NONE) ||
                    (cssv == CSS.Value.DOTTED) ||
                    (cssv == CSS.Value.DASHED) ||
                    (cssv == CSS.Value.SOLID) ||
                    (cssv == CSS.Value.DOUBLE) ||
                    (cssv == CSS.Value.GROOVE) ||
                    (cssv == CSS.Value.RIDGE)) {

                    BorderStyle bs = new BorderStyle();
                    bs.svalue = value;
                    bs.style = cssv;
                    return bs;
                }
            }
            return null;
        }

        @Serial
        private void writeObject(java.io.ObjectOutputStream s)
                     throws IOException {
            s.defaultWriteObject();
            if (style == null) {
                s.writeObject(null);
            }
            else {
                s.writeObject(style.toString());
            }
        }

        @Serial
        private void readObject(ObjectInputStream s)
                throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            Object value = s.readObject();
            if (value != null) {
                style = CSS.getValue((String)value);
            }
        }

        @Override
        public int hashCode() {
            return (style != null) ? style.hashCode() : 0;
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.BorderStyle border && style.equals(border.style);
        }

        // CSS.Values are static, don't archive it.
        private transient CSS.Value style;
    }

    @SuppressWarnings("serial") // Same-version serialization only
    static class LengthValue extends CssValue {

        /**
         * if this length value may be negative.
         */
        boolean mayBeNegative;

        LengthValue() {
            this(false);
        }

        LengthValue(boolean mayBeNegative) {
            this.mayBeNegative = mayBeNegative;
        }

        /**
         * Returns the length (span) to use.
         */
        float getValue() {
            return getValue(false);
        }

        float getValue(boolean isW3CLengthUnits) {
            return getValue(0, isW3CLengthUnits);
        }

        /**
         * Returns the length (span) to use. If the value represents
         * a percentage, it is scaled based on {@code currentValue}.
         */
        float getValue(float currentValue) {
            return getValue(currentValue, false);
        }
        float getValue(float currentValue, boolean isW3CLengthUnits) {
            if (percentage) {
                return span * currentValue;
            }
            return LengthUnit.getValue(span, units, isW3CLengthUnits);
        }

        /**
         * Returns true if the length represents a percentage of the
         * containing box.
         */
        boolean isPercentage() {
            return percentage;
        }

        Object parseCssValue(String value) {
            LengthValue lv;
            try {
                // Assume pixels
                float absolute = Float.parseFloat(value);
                lv = new LengthValue();
                lv.span = absolute;
            } catch (NumberFormatException nfe) {
                // Not pixels, use LengthUnit
                LengthUnit lu = new LengthUnit(value,
                                               LengthUnit.UNINITIALIZED_LENGTH,
                                               0);

                // PENDING: currently, we only support absolute values and
                // percentages.
                switch (lu.type) {
                case 0:
                    // Absolute
                    lv = new LengthValue();
                    lv.span =
                        (mayBeNegative) ? lu.value : Math.max(0, lu.value);
                    lv.units = lu.units;
                    break;
                case 1:
                    // %
                    lv = new LengthValue();
                    lv.span = Math.max(0, lu.value);
                    lv.percentage = true;
                    break;
                default:
                    return null;
                }
            }
            lv.svalue = value;
            return lv;
        }

        Object parseHtmlValue(String value) {
            if (value.equals(HTML.NULL_ATTRIBUTE_VALUE)) {
                value = "1";
            }
            return parseCssValue(value);
        }
        /**
         * Converts a {@code StyleConstants} attribute value to
         * a CSS attribute value.  If there is no conversion,
         * returns {@code null}.  By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @param value the value of a {@code StyleConstants}
         *   attribute to be converted
         * @return the CSS value that represents the
         *   {@code StyleConstants} value
         */
        Object fromStyleConstants(StyleConstants key, Object value) {
            LengthValue v = new LengthValue();
            v.svalue = value.toString();
            v.span = ((Float)value).floatValue();
            return v;
        }

        /**
         * Converts a CSS attribute value to a {@code StyleConstants}
         * value.  If there is no conversion, returns {@code null}.
         * By default, there is no conversion.
         *
         * @param key the {@code StyleConstants} attribute
         * @return the {@code StyleConstants} attribute value that
         *   represents the CSS attribute value
         */
        Object toStyleConstants(StyleConstants key, View v) {
            return Float.valueOf(getValue(false));
        }

        @Override
        public int hashCode() {
            return Float.hashCode(span)
                   | Boolean.hashCode(percentage)
                   | Objects.hashCode(units);
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.LengthValue lu
                   && percentage == lu.percentage
                   && span == lu.span
                   && Objects.equals(units, lu.units);
        }

        /** If true, span is a percentage value, and that to determine
         * the length another value needs to be passed in. */
        boolean percentage;

        /** Either the absolute value (percentage == false) or
         * a percentage value. */
        float span;

        String units = null;
    }


    /**
     * BorderWidthValue is used to model BORDER_XXX_WIDTH and adds support
     * for the thin/medium/thick values.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class BorderWidthValue extends LengthValue {
        BorderWidthValue(String svalue, int index) {
            this.svalue = svalue;
            span = values[index];
            percentage = false;
        }

        Object parseCssValue(String value) {
            if (value != null) {
                if (value.equals("thick")) {
                    return new BorderWidthValue(value, 2);
                }
                else if (value.equals("medium")) {
                    return new BorderWidthValue(value, 1);
                }
                else if (value.equals("thin")) {
                    return new BorderWidthValue(value, 0);
                }
            }
            // Assume its a length.
            return super.parseCssValue(value);
        }

        Object parseHtmlValue(String value) {
            if (value == HTML.NULL_ATTRIBUTE_VALUE) {
                return parseCssValue("medium");
            }
            return parseCssValue(value);
        }

        /** Values used to represent border width. */
        private static final float[] values = { 1, 2, 4 };
   }


    /**
     * Handles uniquing of CSS values, like lists, and background image
     * repeating.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class CssValueMapper extends CssValue {
        Object parseCssValue(String value) {
            Object retValue = cssValueToInternalValueMap.get(value);
            if (retValue == null) {
                retValue = cssValueToInternalValueMap.get(value.toLowerCase());
            }
            return retValue;
        }


        Object parseHtmlValue(String value) {
            Object retValue = htmlValueToCssValueMap.get(value);
            if (retValue == null) {
                retValue = htmlValueToCssValueMap.get(value.toLowerCase());
            }
            return retValue;
        }
    }


    /**
     * Used for background images, to represent the position.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class BackgroundPosition extends CssValue {
        float horizontalPosition;
        float verticalPosition;
        // bitmask: bit 0, horizontal relative, bit 1 horizontal relative to
        // font size, 2 vertical relative to size, 3 vertical relative to
        // font size.
        //
        short relative;

        Object parseCssValue(String value) {
            // 'top left' and 'left top' both mean the same as '0% 0%'.
            // 'top', 'top center' and 'center top' mean the same as '50% 0%'.
            // 'right top' and 'top right' mean the same as '100% 0%'.
            // 'left', 'left center' and 'center left' mean the same as
            //        '0% 50%'.
            // 'center' and 'center center' mean the same as '50% 50%'.
            // 'right', 'right center' and 'center right' mean the same as
            //        '100% 50%'.
            // 'bottom left' and 'left bottom' mean the same as '0% 100%'.
            // 'bottom', 'bottom center' and 'center bottom' mean the same as
            //        '50% 100%'.
            // 'bottom right' and 'right bottom' mean the same as '100% 100%'.
            String[]  strings = CSS.parseStrings(value);
            int count = strings.length;
            BackgroundPosition bp = new BackgroundPosition();
            bp.relative = 5;
            bp.svalue = value;

            if (count > 0) {
                // bit 0 for vert, 1 hor, 2 for center
                short found = 0;
                int index = 0;
                while (index < count) {
                    // First, check for keywords
                    String string = strings[index++];
                    if (string.equals("center")) {
                        found |= 4;
                        continue;
                    }
                    else {
                        if ((found & 1) == 0) {
                            if (string.equals("top")) {
                                found |= 1;
                            }
                            else if (string.equals("bottom")) {
                                found |= 1;
                                bp.verticalPosition = 1;
                                continue;
                            }
                        }
                        if ((found & 2) == 0) {
                            if (string.equals("left")) {
                                found |= 2;
                                bp.horizontalPosition = 0;
                            }
                            else if (string.equals("right")) {
                                found |= 2;
                                bp.horizontalPosition = 1;
                            }
                        }
                    }
                }
                if (found != 0) {
                    if ((found & 1) == 1) {
                        if ((found & 2) == 0) {
                            // vert and no horiz.
                            bp.horizontalPosition = .5f;
                        }
                    }
                    else if ((found & 2) == 2) {
                        // horiz and no vert.
                        bp.verticalPosition = .5f;
                    }
                    else {
                        // no horiz, no vert, but center
                        bp.horizontalPosition = bp.verticalPosition = .5f;
                    }
                }
                else {
                    // Assume lengths
                    LengthUnit lu = new LengthUnit(strings[0], (short)0, 0f);

                    if (lu.type == 0) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)(1 ^ bp.relative);
                    }
                    else if (lu.type == 1) {
                        bp.horizontalPosition = lu.value;
                    }
                    else if (lu.type == 3) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)((1 ^ bp.relative) | 2);
                    }
                    if (count > 1) {
                        lu = new LengthUnit(strings[1], (short)0, 0f);

                        if (lu.type == 0) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)(4 ^ bp.relative);
                        }
                        else if (lu.type == 1) {
                            bp.verticalPosition = lu.value;
                        }
                        else if (lu.type == 3) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)((4 ^ bp.relative) | 8);
                        }
                    }
                    else {
                        bp.verticalPosition = .5f;
                    }
                }
            }
            return bp;
        }

        boolean isHorizontalPositionRelativeToSize() {
            return ((relative & 1) == 1);
        }

        boolean isHorizontalPositionRelativeToFontSize() {
            return ((relative & 2) == 2);
        }

        float getHorizontalPosition() {
            return horizontalPosition;
        }

        boolean isVerticalPositionRelativeToSize() {
            return ((relative & 4) == 4);
        }

        boolean isVerticalPositionRelativeToFontSize() {
            return ((relative & 8) == 8);
        }

        float getVerticalPosition() {
            return verticalPosition;
        }

        @Override
        public int hashCode() {
            return Float.hashCode(horizontalPosition)
                   | Float.hashCode(verticalPosition)
                   | Short.hashCode(relative);
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.BackgroundPosition bp
                    && horizontalPosition == bp.horizontalPosition
                    && verticalPosition == bp.verticalPosition
                    && relative == bp.relative;
        }
    }


    /**
     * Used for BackgroundImages.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class BackgroundImage extends CssValue {
        private volatile boolean loadedImage;
        private ImageIcon image;

        Object parseCssValue(String value) {
            BackgroundImage retValue = new BackgroundImage();
            retValue.svalue = value;
            return retValue;
        }

        Object parseHtmlValue(String value) {
            return parseCssValue(value);
        }

        // PENDING: this base is wrong for linked style sheets.
        ImageIcon getImage(URL base) {
            if (!loadedImage) {
                synchronized(this) {
                    if (!loadedImage) {
                        URL url = CSS.getURL(base, svalue);
                        if (url != null) {
                            image = new ImageIcon();
                            Image tmpImg = Toolkit.getDefaultToolkit().createImage(url);
                            if (tmpImg != null) {
                                image.setImage(tmpImg);
                            }
                        }
                        loadedImage = true;
                    }
                }
            }
            return image;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(svalue);
        }

        @Override
        public boolean equals(Object val) {
            return val instanceof CSS.BackgroundImage img
                   && Objects.equals(svalue, img.svalue);
        }
    }

    /**
     * Parses a length value, this is used internally, and never added
     * to an AttributeSet or returned to the developer.
     */
    @SuppressWarnings("serial") // Same-version serialization only
    static class LengthUnit implements Serializable {
        static Hashtable<String, Float> lengthMapping = new Hashtable<String, Float>(6);
        static Hashtable<String, Float> w3cLengthMapping = new Hashtable<String, Float>(6);
        static {
            lengthMapping.put("pt", 1f);
            // Not sure about 1.3, determined by experimentation.
            lengthMapping.put("px", 1.3f);
            lengthMapping.put("mm", 2.83464f);
            lengthMapping.put("cm", 28.3464f);
            lengthMapping.put("pc", 12f);
            lengthMapping.put("in", 72f);
            // Mapping according to the CSS2.2 spec
            // https://www.w3.org/TR/CSS22/syndata.html#x39
            w3cLengthMapping.put("pt", 96f / 72f);         // 1/72 of 1in
            w3cLengthMapping.put("px", 1f);                // 1/96 of 1in
            w3cLengthMapping.put("mm", 96f / 2.54f / 10f); // 1/10 of 1cm
            w3cLengthMapping.put("cm", 96f / 2.54f);       // 96px/2.54
            w3cLengthMapping.put("pc", 96f / 6f);          // 1/6 of 1in
            w3cLengthMapping.put("in", 96f);               // 96px
        }

        LengthUnit(String value, short defaultType, float defaultValue) {
            parse(value, defaultType, defaultValue);
        }

        void parse(String value, short defaultType, float defaultValue) {
            type = defaultType;
            this.value = defaultValue;

            int length = value.length();
            if (length < 1) {
                return;
            }
            if (value.charAt(length - 1) == '%') {
                try {
                    this.value = Float.parseFloat(value.substring(0, length - 1)) / 100.0f;
                    type = 1;
                }
                catch (NumberFormatException nfe) { }
            }
            else if (length >= 2) {
                units = value.substring(length - 2, length);
                Float scale = lengthMapping.get(units);
                if (scale != null) {
                    try {
                        this.value = Float.parseFloat(value.substring(0, length - 2));
                        type = 0;
                    }
                    catch (NumberFormatException nfe) { }
                }
                else if (units.equals("em") ||
                         units.equals("ex")) {
                    try {
                        this.value = Float.parseFloat(value.substring(0, length - 2));
                        type = 3;
                    }
                    catch (NumberFormatException nfe) { }
                }
                else if (value.equals("larger")) {
                    this.value = 2.f;
                    type = 2;
                }
                else if (value.equals("smaller")) {
                    this.value = -2.f;
                    type = 2;
                }
                else {
                    // treat like points.
                    try {
                        this.value = Float.parseFloat(value);
                        type = 0;
                    } catch (NumberFormatException nfe) {}
                }
            }
            else {
                // treat like points.
                try {
                    this.value = Float.parseFloat(value);
                    type = 0;
                } catch (NumberFormatException nfe) {}
            }
        }

        float getValue(boolean w3cLengthUnits) {
            Hashtable<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
            float scale = 1;
            if (units != null) {
                Float scaleFloat = mapping.get(units);
                if (scaleFloat != null) {
                    scale = scaleFloat.floatValue();
                }
            }
            return this.value * scale;

        }

        static float getValue(float value, String units, Boolean w3cLengthUnits) {
            Hashtable<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
            float scale = 1;
            if (units != null) {
                Float scaleFloat = mapping.get(units);
                if (scaleFloat != null) {
                    scale = scaleFloat.floatValue();
                }
            }
            return value * scale;
        }

        public String toString() {
            return type + " " + value;
        }

        @Override
        public int hashCode() {
            return Float.hashCode(value)
                   | Short.hashCode(type)
                   | Objects.hashCode(units);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LengthUnit lu
                   && type == lu.type
                   && value == lu.value
                   && Objects.equals(units, lu.units);
        }

        // 0 - value indicates real value
        // 1 - % value, value relative to depends upon key.
        //     50% will have a value = .5
        // 2 - add value to parent value.
        // 3 - em/ex relative to font size of element (except for
        //     font-size, which is relative to parent).
        short type;
        float value;
        String units = null;


        static final short UNINITIALIZED_LENGTH = (short)10;
    }


    /**
     * Class used to parse font property. The font property is shorthand
     * for the other font properties. This expands the properties, placing
     * them in the attributeset.
     */
    static class ShorthandFontParser {
        /**
         * Parses the shorthand font string {@code value}, placing the
         * result in {@code attr}.
         */
        static void parseShorthandFont(CSS css, String value,
                                       MutableAttributeSet attr) {
            // font is of the form:
            // [ <font-style> || <font-variant> || <font-weight> ]? <font-size>
            //   [ / <line-height> ]? <font-family>
            String[]   strings = CSS.parseStrings(value);
            int        count = strings.length;
            int        index = 0;
            // bitmask, 1 for style, 2 for variant, 3 for weight
            short      found = 0;
            int        maxC = Math.min(3, count);

            // Check for font-style font-variant font-weight
            while (index < maxC) {
                if ((found & 1) == 0 && isFontStyle(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_STYLE,
                                            strings[index++]);
                    found |= 1;
                }
                else if ((found & 2) == 0 && isFontVariant(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_VARIANT,
                                            strings[index++]);
                    found |= 2;
                }
                else if ((found & 4) == 0 && isFontWeight(strings[index])) {
                    css.addInternalCSSValue(attr, CSS.Attribute.FONT_WEIGHT,
                                            strings[index++]);
                    found |= 4;
                }
                else if (strings[index].equals("normal")) {
                    index++;
                }
                else {
                    break;
                }
            }
            if ((found & 1) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_STYLE,
                                        "normal");
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_VARIANT,
                                        "normal");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_WEIGHT,
                                        "normal");
            }

            // string at index should be the font-size
            if (index < count) {
                String fontSize = strings[index];
                int slashIndex = fontSize.indexOf('/');

                if (slashIndex != -1) {
                    fontSize = fontSize.substring(0, slashIndex);
                    strings[index] = strings[index].substring(slashIndex);
                }
                else {
                    index++;
                }
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_SIZE,
                                        fontSize);
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_SIZE,
                                        "medium");
            }

            // Check for line height
            if (index < count && strings[index].startsWith("/")) {
                String lineHeight = null;
                if (strings[index].equals("/")) {
                    if (++index < count) {
                        lineHeight = strings[index++];
                    }
                }
                else {
                    lineHeight = strings[index++].substring(1);
                }
                // line height
                if (lineHeight != null) {
                    css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                            lineHeight);
                }
                else {
                    css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                            "normal");
                }
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.LINE_HEIGHT,
                                        "normal");
            }

            // remainder of strings are font-family
            if (index < count) {
                String family = strings[index++];

                while (index < count) {
                    family += " " + strings[index++];
                }
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_FAMILY,
                                        family);
            }
            else {
                css.addInternalCSSValue(attr, CSS.Attribute.FONT_FAMILY,
                                        Font.SANS_SERIF);
            }
        }

        private static boolean isFontStyle(String string) {
            return (string.equals("italic") ||
                    string.equals("oblique"));
        }

        private static boolean isFontVariant(String string) {
            return (string.equals("small-caps"));
        }

        private static boolean isFontWeight(String string) {
            if (string.equals("bold") || string.equals("bolder") ||
                string.equals("italic") || string.equals("lighter")) {
                return true;
            }
            // test for 100-900
            return (string.length() == 3 &&
                    string.charAt(0) >= '1' && string.charAt(0) <= '9' &&
                    string.charAt(1) == '0' && string.charAt(2) == '0');
        }

    }


    /**
     * Parses the background property into its intrinsic values.
     */
    static class ShorthandBackgroundParser {
        /**
         * Parses the shorthand font string {@code value}, placing the
         * result in {@code attr}.
         */
        static void parseShorthandBackground(CSS css, String value,
                                             MutableAttributeSet attr) {
            String[] strings = parseStrings(value);
            int count = strings.length;
            int index = 0;
            // bitmask: 0 for image, 1 repeat, 2 attachment, 3 position,
            //          4 color
            short found = 0;

            while (index < count) {
                String string = strings[index++];
                if ((found & 1) == 0 && isImage(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_IMAGE, string);
                    found |= 1;
                }
                else if ((found & 2) == 0 && isRepeat(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_REPEAT, string);
                    found |= 2;
                }
                else if ((found & 4) == 0 && isAttachment(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_ATTACHMENT, string);
                    found |= 4;
                }
                else if ((found & 8) == 0 && isPosition(string)) {
                    if (index < count && isPosition(strings[index])) {
                        css.addInternalCSSValue(attr, CSS.Attribute.
                                                BACKGROUND_POSITION,
                                                string + " " +
                                                strings[index++]);
                    }
                    else {
                        css.addInternalCSSValue(attr, CSS.Attribute.
                                                BACKGROUND_POSITION, string);
                    }
                    found |= 8;
                }
                else if ((found & 16) == 0 && isColor(string)) {
                    css.addInternalCSSValue(attr, CSS.Attribute.
                                            BACKGROUND_COLOR, string);
                    found |= 16;
                }
            }
            if ((found & 1) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.BACKGROUND_IMAGE,
                                        null);
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.BACKGROUND_REPEAT,
                                        "repeat");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.
                                        BACKGROUND_ATTACHMENT, "scroll");
            }
            if ((found & 8) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.
                                        BACKGROUND_POSITION, null);
            }
            // Currently, there is no good way to express this.
            /*
            if ((found & 16) == 0) {
                css.addInternalCSSValue(attr, CSS.Attribute.BACKGROUND_COLOR,
                                        null);
            }
            */
        }

        static boolean isImage(String string) {
            return (string.startsWith("url(") && string.endsWith(")"));
        }

        static boolean isRepeat(String string) {
            return (string.equals("repeat-x") || string.equals("repeat-y") ||
                    string.equals("repeat") || string.equals("no-repeat"));
        }

        static boolean isAttachment(String string) {
            return (string.equals("fixed") || string.equals("scroll"));
        }

        static boolean isPosition(String string) {
            return (string.equals("top") || string.equals("bottom") ||
                    string.equals("left") || string.equals("right") ||
                    string.equals("center") ||
                    (string.length() > 0 &&
                     Character.isDigit(string.charAt(0))));
        }

        static boolean isColor(String string) {
            return (CSS.stringToColor(string) != null);
        }
    }


    /**
     * Used to parser margin and padding.
     */
    static class ShorthandMarginParser {
        /**
         * Parses the shorthand margin/padding/border string
         * {@code value}, placing the result in {@code attr}.
         * {@code names} give the 4 intrinsic property names.
         */
        static void parseShorthandMargin(CSS css, String value,
                                         MutableAttributeSet attr,
                                         CSS.Attribute[] names) {
            String[] strings = parseStrings(value);
            int count = strings.length;
            int index = 0;
            switch (count) {
            case 0:
                // empty string
                return;
            case 1:
                // Identifies all values.
                for (int counter = 0; counter < 4; counter++) {
                    css.addInternalCSSValue(attr, names[counter], strings[0]);
                }
                break;
            case 2:
                // 0 & 2 = strings[0], 1 & 3 = strings[1]
                css.addInternalCSSValue(attr, names[0], strings[0]);
                css.addInternalCSSValue(attr, names[2], strings[0]);
                css.addInternalCSSValue(attr, names[1], strings[1]);
                css.addInternalCSSValue(attr, names[3], strings[1]);
                break;
            case 3:
                css.addInternalCSSValue(attr, names[0], strings[0]);
                css.addInternalCSSValue(attr, names[1], strings[1]);
                css.addInternalCSSValue(attr, names[2], strings[2]);
                css.addInternalCSSValue(attr, names[3], strings[1]);
                break;
            default:
                for (int counter = 0; counter < 4; counter++) {
                    css.addInternalCSSValue(attr, names[counter],
                                            strings[counter]);
                }
                break;
            }
        }
    }

    static class ShorthandBorderParser {
        static Attribute[] keys = {
            Attribute.BORDER_TOP, Attribute.BORDER_RIGHT,
            Attribute.BORDER_BOTTOM, Attribute.BORDER_LEFT,
        };

        static void parseShorthandBorder(MutableAttributeSet attributes,
                                            CSS.Attribute key, String value) {
            Object[] parts = new Object[CSSBorder.PARSERS.length];
            String[] strings = parseStrings(value);
            for (String s : strings) {
                boolean valid = false;
                for (int i = 0; i < parts.length; i++) {
                    Object v = CSSBorder.PARSERS[i].parseCssValue(s);
                    if (v != null) {
                        if (parts[i] == null) {
                            parts[i] = v;
                            valid = true;
                        }
                        break;
                    }
                }
                if (!valid) {
                    // Part is non-parseable or occurred more than once.
                    return;
                }
            }

            // Unspecified parts get default values.
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] == null) {
                    parts[i] = CSSBorder.DEFAULTS[i];
                }
            }

            // Dispatch collected values to individual properties.
            for (int i = 0; i < keys.length; i++) {
                if ((key == Attribute.BORDER) || (key == keys[i])) {
                    for (int k = 0; k < parts.length; k++) {
                        attributes.addAttribute(
                                        CSSBorder.ATTRIBUTES[k][i], parts[k]);
                    }
                }
            }
        }
    }

    /**
     * Calculate the requirements needed to tile the requirements
     * given by the iterator that would be tiled.  The calculation
     * takes into consideration margin and border spacing.
     */
    static SizeRequirements calculateTiledRequirements(LayoutIterator iter, SizeRequirements r) {
        long minimum = 0;
        long maximum = 0;
        long preferred = 0;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int) iter.getLeadingCollapseSpan();
            totalSpacing += Math.max(margin0, margin1);
            preferred += (int) iter.getPreferredSpan(0);
            minimum += iter.getMinimumSpan(0);
            maximum += iter.getMaximumSpan(0);

            lastMargin = (int) iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing += 2 * iter.getBorderWidth();

        // adjust for the spacing area
        minimum += totalSpacing;
        preferred += totalSpacing;
        maximum += totalSpacing;

        // set return value
        if (r == null) {
            r = new SizeRequirements();
        }
        r.minimum = (minimum > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)minimum;
        r.preferred = (preferred > Integer.MAX_VALUE) ? Integer.MAX_VALUE :(int) preferred;
        r.maximum = (maximum > Integer.MAX_VALUE) ? Integer.MAX_VALUE :(int) maximum;
        return r;
    }

    /**
     * Calculate a tiled layout for the given iterator.
     * This should be done collapsing the neighboring
     * margins to be a total of the maximum of the two
     * neighboring margin areas as described in the CSS spec.
     */
    static void calculateTiledLayout(LayoutIterator iter, int targetSpan) {

        /*
         * first pass, calculate the preferred sizes, adjustments needed because
         * of margin collapsing, and the flexibility to adjust the sizes.
         */
        long preferred = 0;
        long currentPreferred;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        int adjustmentWeightsCount = LayoutIterator.WorstAdjustmentWeight + 1;
        //max gain we can get adjusting elements with adjustmentWeight <= i
        long[] gain = new long[adjustmentWeightsCount];
        //max loss we can get adjusting elements with adjustmentWeight <= i
        long[] loss = new long[adjustmentWeightsCount];

        for (int i = 0; i < adjustmentWeightsCount; i++) {
            gain[i] = loss[i] = 0;
        }
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int) iter.getLeadingCollapseSpan();

            iter.setOffset(Math.max(margin0, margin1));
            totalSpacing += iter.getOffset();

            currentPreferred = (long)iter.getPreferredSpan(targetSpan);
            iter.setSpan((int) currentPreferred);
            preferred += currentPreferred;
            gain[iter.getAdjustmentWeight()] +=
                (long)iter.getMaximumSpan(targetSpan) - currentPreferred;
            loss[iter.getAdjustmentWeight()] +=
                currentPreferred - (long)iter.getMinimumSpan(targetSpan);
            lastMargin = (int) iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing += 2 * iter.getBorderWidth();

        for (int i = 1; i < adjustmentWeightsCount; i++) {
            gain[i] += gain[i - 1];
            loss[i] += loss[i - 1];
        }

        /*
         * Second pass, expand or contract by as much as possible to reach
         * the target span.  This takes the margin collapsing into account
         * prior to adjusting the span.
         */

        // determine the adjustment to be made
        int allocated = targetSpan - totalSpacing;
        long desiredAdjustment = allocated - preferred;
        long[] adjustmentsArray = (desiredAdjustment > 0) ? gain : loss;
        desiredAdjustment = Math.abs(desiredAdjustment);
        int adjustmentLevel = 0;
        for (;adjustmentLevel <= LayoutIterator.WorstAdjustmentWeight;
             adjustmentLevel++) {
            // adjustmentsArray[] is sorted. I do not bother about
            // binary search though
            if (adjustmentsArray[adjustmentLevel] >= desiredAdjustment) {
                break;
            }
        }
        float adjustmentFactor = 0.0f;
        if (adjustmentLevel <= LayoutIterator.WorstAdjustmentWeight) {
            desiredAdjustment -= (adjustmentLevel > 0) ?
                adjustmentsArray[adjustmentLevel - 1] : 0;
            if (desiredAdjustment != 0) {
                float maximumAdjustment =
                    adjustmentsArray[adjustmentLevel] -
                    ((adjustmentLevel > 0) ?
                     adjustmentsArray[adjustmentLevel - 1] : 0
                     );
                adjustmentFactor = desiredAdjustment / maximumAdjustment;
            }
        }
        // make the adjustments
        int totalOffset = (int)iter.getBorderWidth();
        for (int i = 0; i < n; i++) {
            iter.setIndex(i);
            iter.setOffset( iter.getOffset() + totalOffset);
            if (iter.getAdjustmentWeight() < adjustmentLevel) {
                iter.setSpan((int)
                             ((allocated > preferred) ?
                              Math.floor(iter.getMaximumSpan(targetSpan)) :
                              Math.ceil(iter.getMinimumSpan(targetSpan))
                              )
                             );
            } else if (iter.getAdjustmentWeight() == adjustmentLevel) {
                int availableSpan = (allocated > preferred) ?
                    (int) iter.getMaximumSpan(targetSpan) - iter.getSpan() :
                    iter.getSpan() - (int) iter.getMinimumSpan(targetSpan);
                int adj = (int)Math.floor(adjustmentFactor * availableSpan);
                iter.setSpan(iter.getSpan() +
                             ((allocated > preferred) ? adj : -adj));
            }
            totalOffset = (int) Math.min((long) iter.getOffset() +
                                         (long) iter.getSpan(),
                                         Integer.MAX_VALUE);
        }

        // while rounding we could lose several pixels.
        int roundError = targetSpan - totalOffset -
            (int)iter.getTrailingCollapseSpan() -
            (int)iter.getBorderWidth();
        int adj = (roundError > 0) ? 1 : -1;
        roundError *= adj;

        boolean canAdjust = true;
        while (roundError > 0 && canAdjust) {
            // check for infinite loop
            canAdjust = false;
            int offsetAdjust = 0;
            // try to distribute roundError. one pixel per cell
            for (int i = 0; i < n; i++) {
                iter.setIndex(i);
                iter.setOffset(iter.getOffset() + offsetAdjust);
                int curSpan = iter.getSpan();
                if (roundError > 0) {
                    int boundGap = (adj > 0) ?
                        (int)Math.floor(iter.getMaximumSpan(targetSpan)) - curSpan :
                        curSpan - (int)Math.ceil(iter.getMinimumSpan(targetSpan));
                    if (boundGap >= 1) {
                        canAdjust = true;
                        iter.setSpan(curSpan + adj);
                        offsetAdjust += adj;
                        roundError--;
                    }
                }
            }
        }
    }

    /**
     * An iterator to express the requirements to use when computing
     * layout.
     */
    interface LayoutIterator {

        void setOffset(int offs);

        int getOffset();

        void setSpan(int span);

        int getSpan();

        int getCount();

        void setIndex(int i);

        float getMinimumSpan(float parentSpan);

        float getPreferredSpan(float parentSpan);

        float getMaximumSpan(float parentSpan);

        int getAdjustmentWeight(); //0 is the best weight WorstAdjustmentWeight is a worst one

        //float getAlignment();

        float getBorderWidth();

        float getLeadingCollapseSpan();

        float getTrailingCollapseSpan();
        public static final int WorstAdjustmentWeight = 2;
    }

    //
    // Serialization support
    //

    @Serial
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();

        // Determine what values in valueConvertor need to be written out.
        Enumeration<?> keys = valueConvertor.keys();
        s.writeInt(valueConvertor.size());
        if (keys != null) {
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = valueConvertor.get(key);
                if (!(key instanceof Serializable) &&
                    (key = StyleContext.getStaticAttributeKey(key)) == null) {
                    // Should we throw an exception here?
                    key = null;
                    value = null;
                }
                else if (!(value instanceof Serializable) &&
                    (value = StyleContext.getStaticAttributeKey(value)) == null){
                    // Should we throw an exception here?
                    key = null;
                    value = null;
                }
                s.writeObject(key);
                s.writeObject(value);
            }
        }
    }

    @Serial
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();
        int newBaseFontSize = f.get("baseFontSize", 0);
        setBaseFontSize(newBaseFontSize);

        // Reconstruct the hashtable.
        int numValues = s.readInt();
        valueConvertor = new Hashtable<>();
        while (numValues-- > 0) {
            Object key = s.readObject();
            Object value = s.readObject();
            Object staticKey = StyleContext.getStaticAttribute(key);
            if (staticKey != null) {
                key = staticKey;
            }
            Object staticValue = StyleContext.getStaticAttribute(value);
            if (staticValue != null) {
                value = staticValue;
            }
            if (key != null && value != null) {
                valueConvertor.put(key, value);
            }
        }
    }


    /*
     * we need StyleSheet for resolving length units. (see
     * isW3CLengthUnits)
     * we can not pass stylesheet for handling relative sizes. (do not
     * think changing public API is necessary)
     * CSS is not likely to be accessed from more then one thread.
     * Having local storage for StyleSheet for resolving relative
     * sizes is safe
     *
     * idk 08/30/2004
     */
    private StyleSheet getStyleSheet(StyleSheet ss) {
        if (ss != null) {
            styleSheet = ss;
        }
        return styleSheet;
    }
    //
    // Instance variables
    //

    /** Maps from CSS key to CssValue. */
    private transient Hashtable<Object, Object> valueConvertor;

    /** Size used for relative units. */
    private int baseFontSize;

    private transient StyleSheet styleSheet = null;

    static int baseFontSizeIndex = 3;
}
