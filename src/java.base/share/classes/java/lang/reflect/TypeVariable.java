/*
 * Copyright (c) 2003, 2022, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * TypeVariable is the common superinterface for type variables of kinds.
 * A type variable is created the first time it is needed by a reflective
 * method, as specified in this package.  If a type variable t is referenced
 * by a type (i.e, class, interface or annotation type) T, and T is declared
 * by the n<sup>th</sup> enclosing class of T (see JLS {@jls 8.1.2}), then the creation of t
 * requires the resolution (see JVMS {@jvms 5}) of the i<sup>th</sup> enclosing class of T,
 * for i = 0 to n, inclusive. Creating a type variable must not cause the
 * creation of its bounds. Repeated creation of a type variable has no effect.
 * <p>
 * Two {@code TypeVariable} objects should be compared using the {@link
 * Object#equals equals} method.
 *
 * @param <D> the type of generic declaration that declares this type variable
 *
 * @jls 4.4 Type Variables
 * @since 1.5
 */
public interface TypeVariable<D extends GenericDeclaration> extends Type, AnnotatedElement {
    /**
     * {@return the upper bounds of this type variable}  If no upper bound is
     * explicitly declared, the upper bound is the {@link Object} class.
     *
     * <p>For each upper bound B:
     * <ul>
     *  <li>if B is a parameterized type or a type variable, it is created.
     *  (see {@link ParameterizedType} and {@link TypeVariable} for the details
     *  of the creation process for parameterized types and type variables)
     *  <li>Otherwise, B is resolved.
     * </ul>
     *
     * @throws TypeNotPresentException if any of the bounds refers to a
     *     non-existent type declaration
     * @throws MalformedParameterizedTypeException if any of the bounds refer to
     *     a parameterized type that cannot be instantiated for any reason
     * @jls 4.9 Intersection Types
     */
    Type[] getBounds();

    /**
     * {@return the generic declaration that declares this type variable}
     */
    D getGenericDeclaration();

    /**
     * {@return the name of this type variable, as it appears in the source
     * code}
     */
    String getName();

    /**
     * {@return the potentially annotated uses of upper bounds of the type
     * variable}  They are ordered as they appear in the declaration of the
     * type parameter in the source code.  If no bound is explicitly declared,
     * this method returns an array containing exactly the unannotated use of
     * the {@code Object} class.
     *
     * @throws TypeNotPresentException if any of the bounds refers to a
     *     non-existent type declaration
     * @throws MalformedParameterizedTypeException if any of the bounds refer to
     *     a parameterized type that cannot be instantiated for any reason
     * @jls 4.9 Intersection Types
     * @since 1.8
     */
    AnnotatedType[] getAnnotatedBounds();
}
