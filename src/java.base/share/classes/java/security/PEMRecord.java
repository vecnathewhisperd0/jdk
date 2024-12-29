/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

import java.nio.charset.StandardCharsets;

/**
 *
 * {@code PEMRecord} is a {@link DEREncodable} that stores Privacy-Enhanced Mail
 * (PEM) data and can be used with all PEM data types.  It serves as the default
 * decoding class when the PEM data lacks a Java API cryptographic
 * representation. Types with representation, such as a {@link PrivateKey},
 * can return a {@code PEMRecord} when used with
 * {@linkplain PEMDecoder#decode(String, Class)}. Using {@code PEMRecord} can
 * be helpful when generating a representation is not desired or when used
 * with {@code leadingData}.
 * <p>
 * {@code PEMRecord} may have a null {@code type} and {@code pem} when
 * {@code PEMDecoder.decode()} methods encounter only non-PEM data and has
 * reached the end of the stream.
 * If there is PEM data, {@code type} and {@code pem} will both be non-null.
 * {@code leadingData} may be null if the input data only contains PEM data.
 * All values can never be null.
 *
 * During the instantiation of this record, there is no validation for the
 * {@code type} or {@code pem}.
 * There is no validity checking for {@code type} or {@code pem} during
 * instantiation of this record.
 *
 * @param type The type identifier in the PEM header.  For a public key,
 * {@code type} would be "PUBLIC KEY".
 * @param pem Any data between the PEM header and footer.
 * @param leadingData Any non-PEM data read during the decoding process
 * before the PEM header. This can be useful when reading metadata that
 * accompanies PEM data.
 *
 */
public record PEMRecord(String type, String pem, byte[] leadingData)
    implements DEREncodable {

    /**
     * Return a PEMRecord instance with the given parameters.
     *
     * When {@code type} is given a properly formatted PEM header, only the
     * identifier will be set (ie: {@code PUBLIC KEY}.  Otherwise, {@code type}
     * will be set to what was passed in.
     *
     * When {@code type} is given a correctly formatted PEM header, only the
     * identifier is set (for example, {@code PUBLIC KEY}). Otherwise,
     * {@code type} is set to the value that was passed in.
     *
     * @param type The type identifier in the PEM header and footer.
     *             If there is no PEM data, this value will be {@code null}.
     * @param pem The data between the PEM header and footer.
     * @param leadingData Any non-PEM data read during the decoding process
     *                    before the PEM header.  This value maybe {@code null}.
     */
    public PEMRecord(String type, String pem, byte[] leadingData) {

        if (type == null && pem == null && leadingData == null) {
            throw new IllegalArgumentException("All values may not be null.");
        }

        if (type == null && pem != null || type != null && pem == null) {
            throw new IllegalArgumentException("\"type\" and \"pem\" must be" +
                " both null or non-null");
        }

        // With no validity checking on `type`, the constructor accept anything
        // including lowercase.  The onus is on the caller.
        if (type != null && type.startsWith("-----")) {
            // Remove PEM headers syntax if present.
            this.type = type.substring(11, type.lastIndexOf('-') - 4);
        } else {
            this.type = type;
        }

        this.pem = pem;
        this.leadingData = leadingData;
    }

    /**
     * Returns a PEMRecord instance with a given {@code type} and {@code pem}
     * data in String form.  {@code leadingData} is set to null.
     *
     * @param type The type identifier in the PEM header and footer.
     *             If there is no PEM data, this value will be {@code null}.
     * @param pem The data between the PEM header and footer.
     *
     * @see #PEMRecord(String, String, byte[])
     */
    public PEMRecord(String type, String pem) {
        this(type, pem, null);
    }

    /**
     * Returns a PEMRecord instance with a given String {@code type} and
     * byte array {@code pem}.  {@code leadingData} is set to null.
     *
     * @param type The type identifier in the PEM header and footer.
     *             If there is no PEM data, this value will be {@code null}.
     * @param pem The data between the PEM header and footer.
     *
     * @see #PEMRecord(String, String, byte[])
     */
    public PEMRecord(String type, byte[] pem) {
        this(type, new String(pem, StandardCharsets.ISO_8859_1), null);
    }
}
