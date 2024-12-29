/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */

#include "precompiled.hpp"
#include "opto/rangeinference.hpp"
#include "opto/type.hpp"
#include "utilities/tuple.hpp"

// If the cardinality of a TypeInt is below this threshold, use min widen, see
// TypeIntPrototype<S, U>::normalize_widen
constexpr juint SMALL_TYPEINT_THRESHOLD = 3;

// This represents the result of an iterative calculation
template <class T>
class AdjustResult {
public:
  bool _progress;             // whether there is progress compared to the last iteration
  bool _is_result_consistent; // whether the calculation arrives at contradiction
  T _result;

  static AdjustResult<T> make_empty() {
    return {true, false, {}};
  }
};

// In the canonical form, [lo, hi] intersects with [ulo, uhi] can result in 2
// cases:
// - [lo, hi] is the same as [ulo, uhi], lo and hi are both >= 0 or both < 0.
// - [lo, hi] is not the same as [ulo, uhi], which results in the intersections
// being [lo, uhi] and [ulo, hi], lo and uhi are < 0 while ulo and hi are >= 0.
// This class deals with each interval with both bounds being >= 0 or < 0 in
// the signed domain.
template <class U>
class SimpleCanonicalResult {
  static_assert(std::is_unsigned<U>::value, "bit info should be unsigned");
public:
  bool _present; // whether this is an empty set
  RangeInt<U> _bounds;
  KnownBits<U> _bits;

  static SimpleCanonicalResult<U> make_empty() {
    return {false, {}, {}};
  }
};

// Find the minimum value that is not less than lo and satisfies bits. If there
// does not exist one such number, the calculation will overflow and return a
// value < lo.
//
// Here, we view a number in binary as a bit string. As a result,  the first
// bit refers to the highest bit (the MSB), the last bit refers to the lowest
// bit (the LSB), a bit comes before (being higher than) another if it is more
// significant, and a bit comes after (being lower than) another if it is less
// significant.
template <class U>
static U adjust_lo(U lo, const KnownBits<U>& bits) {
  constexpr size_t W = sizeof(U) * 8;
  // Violation of lo with respects to bits
  // E.g: lo    = 1100
  //      zeros = 0100
  //      ones  = 1001
  // zero_violation = 0100, i.e the second bit should be zero, but it is 1 in
  // lo. Similarly, one_violation = 0001, i.e the last bit should be one, but
  // it is 0 in lo. These make lo not satisfy the bit constraints, which
  // results in us having to find the smallest value that satisfies bits
  U zero_violation = lo & bits._zeros;
  U one_violation = ~lo & bits._ones;
  if (zero_violation == one_violation) {
    // This means lo does not violate bits, it is the result
    assert(zero_violation == 0, "");
    return lo;
  }

  /*
  1. Intuition:
  Call res the lowest value not smaller than lo that satisfies bits, consider
  the first bit in res that is different from the corresponding bit in lo,
  since res is larger than lo the bit must be 0 in lo and 1 in res. Since res
  must satisify bits the bit must be 0 in zeros. Finally, as res should be the
  smallest value, this bit should be the last one possible.

  E.g:      1 2 3 4 5 6
       lo = 1 0 0 1 1 0
        x = 1 0 1 0 1 0
        y = 0 1 1 1 1 1
  x would be larger than lo since the first different bit is the 3rd one,
  while y is smaller than lo because the first different bit is the 1st bit.
  Next, consider:
       x1 = 1 0 1 0 1 0
       x2 = 1 0 0 1 1 1
  Both x1 and x2 are larger than lo, but x1 > x2 since its first different
  bit from lo is the 3rd one, while with x2 it is the 7th one. As a result,
  if both x1 and x2 satisfy bits, x2 would be closer to our true result.

  2. Formality:
  Call i the largest value such that (with v[0] being the first bit of v, v[1]
  being the second bit of v and so on):

  - lo[x] satisfies bits for 0 <= x < i
  - zeros[i] = 0
  - lo[i] = 0

  Consider v:

  - v[x] = lo[x], for 0 <= x < i
  - v[i] = 1
  - v[x] = ones[x], for j > i

  We will prove that v is the smallest value not smaller than lo that
  satisfies bits.

  Call r the smallest value not smaller than lo that satisfies bits.

  a. Firstly, we prove that r <= v:

  Trivially, lo < v since lo[i] < v[i] and lo[x] == v[x] for x < i.

  As established above, the first (i + 1) bits of v satisfy bits.
  The remaining bits satisfy zeros, since any bit x > i such that zeros[x] == 1, v[x] == ones[x] == 0
  They also satisfy ones, since any bit j > i such that ones[x] == 1, v[x] == ones[x] == 1

  As a result, v > lo and v satisfies bits since all of its bits satisfy bits. Which
  means r <= v since r is the smallest such value.

  b. Secondly, we prove that r >= v. Suppose r < v:

  Since r < v, there must be a bit position j that:

  r[j] == 0, v[j] == 1
  r[x] == v[x], for x < j

  - If j < i
  r[j] == 0, v[j] == lo[j] == 1
  r[x] == v[x] == lo[x], for x < j

  This means r < lo, which contradicts that r >= lo

  - If j == i
  This means that lo[i] == r[i]. Call k the bit position such that:

  r[k] == 1, lo[k] == 0
  r[x] == lo[x], for x < k

  k > i since r[x] == lo[x], for x <= i
  lo[x] satisfies bits for 0 <= x < k
  zeros[k] == 0
  This contradicts the assumption that i being the largest value satisfying such conditions.

  - If j > i:
  ones[j] == v[j] == 1, which contradicts that r satisfies bits.

  All cases lead to contradictions, which mean r < v is incorrect, which means
  that r >= v.

  As a result, r == v, which means the value v having the above form is the
  lowest value not smaller than lo that satisfies bits.

  Our objective now is to find the largest value i that satisfies:
  - lo[x] satisfies bits for 0 <= x < i
  - zeros[i] = 0
  - lo[i] = 0

  Call j the largest value such that lo[x] satisfies bits for 0 <= x < j. This
  means that j is the smallest value such that lo[j] does not satisfy bits. We
  call this the first violation. i then can be computed as the largest value
  <= j such that:

  zeros[i] == lo[i] == 0
  */

  // The algorithm depends on whether the first violation violates zeros or
  // ones, if it violates zeros, we have the bit being 1 in zero_violation and
  // 0 in one_violation. Since all higher bits are 0 in zero_violation and
  // one_violation, we have zero_violation > one_violation. Similarly, if the
  // first violation violates ones, we have zero_violation < one_violation.
  if (zero_violation < one_violation) {
    // This means that the first bit that does not satisfy the bit requirement
    // is a 0 that should be a 1. Obviously, since the bit at that position in
    // ones is 1, the same bit in zeros is 0. Which means this is the value of
    // i we are looking for.
    //
    // E.g:      1 2 3 4 5 6 7 8
    //      lo = 1 0 0 1 0 0 1 0
    //   zeros = 0 0 1 0 0 1 0 0
    //    ones = 0 1 0 0 1 0 1 0
    //   1-vio = 0 1 0 0 1 0 0 0
    //   0-vio = 0 0 0 0 0 0 0 0
    // Since the result must have the 2nd bit set, it must be at least:
    //           1 1 0 0 0 0 0 0
    // This value must satisfy zeros, because all bits before the 2nd bit have
    // already satisfied zeros, and all bits after the 2nd bit are all 0 now.
    // Just OR this value with ones to obtain the final result.

    // first_violation is the position of the violation counting from the
    // lowest bit up (0-based), since i == 2, first_difference == 6
    juint first_violation = W - 1 - count_leading_zeros(one_violation); // 6
    //           0 1 0 0 0 0 0 0
    U alignment = U(1) << first_violation;
    // This is the first value which have the violated bit being 1, which means
    // that the result should not be smaller than this
    //           1 1 0 0 0 0 0 0
    U new_lo = (lo & -alignment) + alignment;
    //           1 1 0 0 1 0 1 0
    new_lo |= bits._ones;
    assert(lo < new_lo, "this case cannot overflow");
    return new_lo;
  } else {
    // This means that the first bit that does not satisfy the bit requirement
    // is a 1 that should be a 0. Trace backward to find i which is the last
    // bit that is 0 in both lo and zeros.
    //
    // E.g:      1 2 3 4 5 6 7 8
    //      lo = 1 0 0 0 1 1 1 0
    //   zeros = 0 0 0 1 0 1 0 0
    //    ones = 1 0 0 0 0 0 1 1
    //   1-vio = 0 0 0 0 0 0 0 1
    //   0-vio = 0 0 0 0 0 1 0 0
    // The first violation is the 6th bit, which should be 0. The 5th cannot be
    // the first different bit we are looking for, because it is already 1, the
    // 4th bit also cannot be, because it must be 0. As a result, the first
    // different bit between the result and lo must be the 3rd bit. As a result,
    // the result must not be smaller than:
    //           1 0 1 0 0 0 0 0
    // This one satisfies zeros so we can use the logic in the previous case to
    // obtain our final result, which is:
    //           1 0 1 0 0 0 1 1

    juint first_violation = W - count_leading_zeros(zero_violation);
    // This mask out all bits from the first violation
    //           1 1 1 1 1 0 0 0
    U find_mask = std::numeric_limits<U>::max() << first_violation;
    //           1 0 0 1 1 1 1 0
    U either = lo | bits._zeros;
    // i is the last bit being 0 in either that stands before the first
    // violation, which is the last set bit of tmp
    //           0 1 1 0 0 0 0 0
    U tmp = ~either & find_mask;
    // i == 2 here, shortcut the calculation instead of explicitly spelling out
    // i
    //           0 0 1 0 0 0 0 0
    U alignment = tmp & (-tmp);
    // Set the bit at i and unset all the bit after, this is the smallest value
    // that satisfies bits._zeros
    //           1 0 1 0 0 0 0 0
    U new_lo = (lo & -alignment) + alignment;
    // Satisfy bits._ones
    //           1 0 1 0 0 0 1 1
    new_lo |= bits._ones;
    assert(lo < new_lo || new_lo == bits._ones, "overflow must return bits._ones");
    return new_lo;
  }
}

// Try to tighten the bound constraints from the known bit information. I.e, we
// find the smallest value not smaller than lo, as well as the largest value
// not larger than hi both of which satisfy bits
// E.g: lo = 0010, hi = 1001
// zeros = 0011
// ones  = 0000
// -> 4-aligned
//
//         0    1    2    3    4    5    6    7    8    9    10
//         0000 0001 0010 0011 0100 0101 0110 0111 1000 1001 1010
// bits:   ok   .    .    .    ok   .    .    .    ok   .    .
// bounds:           lo                                 hi
// adjust:           --------> lo                  hi <---
template <class U>
static AdjustResult<RangeInt<U>>
adjust_bounds_from_bits(const RangeInt<U>& bounds, const KnownBits<U>& bits) {
  U new_lo = adjust_lo(bounds._lo, bits);
  if (new_lo < bounds._lo) {
    // This means we wrapped around, which means no value not less than lo
    // satisfies bits
    return AdjustResult<RangeInt<U>>::make_empty();
  }

  // We need to find the largest value not larger than hi that satisfies bits
  // One possible method is to do similar to adjust_lo, just with the other
  // direction
  // However, we can observe that if v satisfies {bits._zeros, bits._ones},
  // then ~v would satisfy {bits._ones, bits._zeros}. Combine with the fact
  // that ~ is a strictly decreasing function, if new_hi is the largest value
  // not larger than hi that satisfies {bits._zeros, bits._ones}, then ~new_hi
  // is the smallest value not smaller than ~hi that satisfies
  // {bits._ones, bits._zeros}
  U new_hi = ~adjust_lo(~bounds._hi, {bits._ones, bits._zeros});
  if (new_hi > bounds._hi) {
    return AdjustResult<RangeInt<U>>::make_empty();
  }

  bool progress = (new_lo != bounds._lo) || (new_hi != bounds._hi);
  bool present = new_lo <= new_hi;
  return {progress, present, {new_lo, new_hi}};
}

// Try to tighten the known bit constraints from the bound information by
// extracting the common prefix of lo and hi and combining with the current
// bit constraints
// E.g: lo = 010011
//      hi = 010100,
// then all values in [lo, hi] would be
//           010***
template <class U>
static AdjustResult<KnownBits<U>>
adjust_bits_from_bounds(const KnownBits<U>& bits, const RangeInt<U>& bounds) {
  // Find the mask to filter the common prefix, all values between bounds._lo
  // and bounds._hi should share this common prefix in terms of bits
  U mismatch = bounds._lo ^ bounds._hi;
  // Find the first mismatch, all bits before it is the same in bounds._lo and
  // bounds._hi
  U match_mask = mismatch == 0 ? std::numeric_limits<U>::max()
                               : ~(std::numeric_limits<U>::max() >> count_leading_zeros(mismatch));
  // match_mask & bounds._lo is the common prefix, extract zeros and ones from
  // it
  U new_zeros = bits._zeros | (match_mask & ~bounds._lo);
  U new_ones = bits._ones | (match_mask & bounds._lo);
  bool progress = (new_zeros != bits._zeros) || (new_ones != bits._ones);
  bool present = ((new_zeros & new_ones) == 0);
  return {progress, present, {new_zeros, new_ones}};
}

// Try to tighten both the bounds and the bits at the same time
// Iteratively tighten 1 using the other until no progress is made.
// This function converges because at each iteration, some bits that are
// unknown is made known. As there are at most 64 bits, the number of
// iterations should not be larger than 64
template <class U>
static SimpleCanonicalResult<U>
canonicalize_constraints_simple(const RangeInt<U>& bounds, const KnownBits<U>& bits) {
  AdjustResult<KnownBits<U>> nbits = adjust_bits_from_bounds(bits, bounds);
  if (!nbits._is_result_consistent) {
    return SimpleCanonicalResult<U>::make_empty();
  }
  AdjustResult<RangeInt<U>> nbounds{true, true, bounds};
  // Since bits are derived from bounds in the previous iteration and vice
  // versa, if one does not show progress, the other will also not show
  // progress, so we terminate early
  while (true) {
    nbounds = adjust_bounds_from_bits(nbounds._result, nbits._result);
    if (!nbounds._progress || !nbounds._is_result_consistent) {
      return {nbounds._is_result_consistent, nbounds._result, nbits._result};
    }
    nbits = adjust_bits_from_bounds(nbits._result, nbounds._result);
    if (!nbits._progress || !nbits._is_result_consistent) {
      return {nbits._is_result_consistent, nbounds._result, nbits._result};
    }
  }
}

// Tighten all constraints of a TypeIntPrototype to its canonical form.
// i.e the result represents the same set as the input, each bound belongs to
// the set and for each bit position that is not constrained, there exists 2
// values with the bit value at that position being set and unset, respectively,
// such that both belong to the set represented by the constraints.
template <class S, class U>
typename TypeIntPrototype<S, U>::CanonicalizedTypeIntPrototype
TypeIntPrototype<S, U>::canonicalize_constraints() const {
  RangeInt<S> srange = _srange;
  RangeInt<U> urange = _urange;
  // Trivial contradictions
  if (srange._lo > srange._hi ||
      urange._lo > urange._hi ||
      (_bits._zeros & _bits._ones) != 0) {
    return CanonicalizedTypeIntPrototype::make_empty();
  }

  // Trivially canonicalize the bounds so that srange._lo and urange._hi are
  // both < 0 or >= 0. The same for srange._hi and urange._ulo. See TypeInt for
  // detailed explanation.
  if (S(urange._lo) > S(urange._hi)) {
    // This means that S(urange._lo) >= 0 and S(urange._hi) < 0
    if (S(urange._hi) < srange._lo) {
      // This means that there should be no element in the interval
      // [min_S, S(urange._hi)], tighten urange._hi to max_S
      urange._hi = std::numeric_limits<S>::max();
    } else if (S(urange._lo) > srange._hi) {
      // This means that there should be no element in the interval
      // [S(urange._lo), max_S], tighten urange._lo to min_S
      urange._lo = std::numeric_limits<S>::min();
    }
  }

  if (S(urange._lo) <= S(urange._hi)) {
    // [lo, hi] and [ulo, uhi] represent the same range
    urange._lo = MAX2<S>(urange._lo, srange._lo);
    urange._hi = MIN2<S>(urange._hi, srange._hi);
    if (urange._lo > urange._hi) {
      return CanonicalizedTypeIntPrototype::make_empty();
    }

    auto type = canonicalize_constraints_simple(urange, _bits);
    return {type._present, {{S(type._bounds._lo), S(type._bounds._hi)},
                            type._bounds, type._bits}};
  }

  // [lo, hi] intersects with [ulo, uhi] in 2 ranges:
  // [lo, uhi], which consists of negative values
  // [ulo, hi] which consists of non-negative values
  // We process these 2 separately and combine the results
  auto neg_type = canonicalize_constraints_simple({U(srange._lo), urange._hi}, _bits);
  auto pos_type = canonicalize_constraints_simple({urange._lo, U(srange._hi)}, _bits);

  if (!neg_type._present && !pos_type._present) {
    return CanonicalizedTypeIntPrototype::make_empty();
  } else if (!neg_type._present) {
    return {true, {{S(pos_type._bounds._lo), S(pos_type._bounds._hi)},
                   pos_type._bounds, pos_type._bits}};
  } else if (!pos_type._present) {
    return {true, {{S(neg_type._bounds._lo), S(neg_type._bounds._hi)},
                   neg_type._bounds, neg_type._bits}};
  } else {
    return {true, {{S(neg_type._bounds._lo), S(pos_type._bounds._hi)},
                   {pos_type._bounds._lo, neg_type._bounds._hi},
                   {neg_type._bits._zeros & pos_type._bits._zeros, neg_type._bits._ones & pos_type._bits._ones}}};
  }
}

template <class S, class U>
int TypeIntPrototype<S, U>::normalize_widen(int w) const {
  // Certain normalizations keep us sane when comparing types.
  // The 'SMALL_TYPEINT_THRESHOLD' covers constants and also CC and its relatives.
  if (TypeIntHelper::cardinality_from_bounds(_srange, _urange) <= SMALL_TYPEINT_THRESHOLD) {
    return Type::WidenMin;
  }
  if (_srange._lo == std::numeric_limits<S>::min() && _srange._hi == std::numeric_limits<S>::max() &&
      _urange._lo == std::numeric_limits<U>::min() && _urange._hi == std::numeric_limits<U>::max() &&
      _bits._zeros == 0 && _bits._ones == 0) {
    // bottom type
    return Type::WidenMax;
  }
  return w;
}

#ifdef ASSERT
template <class S, class U>
bool TypeIntPrototype<S, U>::contains(S v) const {
  U u = v;
  return v >= _srange._lo && v <= _srange._hi && u >= _urange._lo && u <= _urange._hi && _bits.is_satisfied_by(u);
}

// Verify that this set representation is canonical
template <class S, class U>
void TypeIntPrototype<S, U>::verify_constraints() const {
  // Assert that the bounds cannot be further tightened
  assert(contains(_srange._lo) && contains(_srange._hi) &&
         contains(_urange._lo) && contains(_urange._hi), "");

  // Assert that the bits cannot be further tightened
  if (U(_srange._lo) == _urange._lo) {
    assert(!adjust_bits_from_bounds(_bits, _urange)._progress, "");
  } else {
    RangeInt<U> neg_range{U(_srange._lo), _urange._hi};
    auto neg_bits = adjust_bits_from_bounds(_bits, neg_range);
    assert(neg_bits._is_result_consistent, "");
    assert(!adjust_bounds_from_bits(neg_range, neg_bits._result)._progress, "");

    RangeInt<U> pos_range{_urange._lo, U(_srange._hi)};
    auto pos_bits = adjust_bits_from_bounds(_bits, pos_range);
    assert(pos_bits._is_result_consistent, "");
    assert(!adjust_bounds_from_bits(pos_range, pos_bits._result)._progress, "");

    assert((neg_bits._result._zeros & pos_bits._result._zeros) == _bits._zeros &&
           (neg_bits._result._ones & pos_bits._result._ones) == _bits._ones, "");
  }
}
#endif // ASSERT

template class TypeIntPrototype<jint, juint>;
template class TypeIntPrototype<jlong, julong>;

// Compute the meet of 2 types, when dual is true, we are actually computing the
// join.
template <class CT, class S, class U>
const Type* TypeIntHelper::int_type_xmeet(const CT* i1, const Type* t2, const Type* (*make)(const TypeIntPrototype<S, U>&, int, bool), bool dual) {
  // Perform a fast test for common case; meeting the same types together.
  if (i1 == t2 || t2 == Type::TOP) {
    return i1;
  }
  const CT* i2 = t2->try_cast<CT>();
  if (i2 != nullptr) {
    if (!dual) {
    // meet
      return make(TypeIntPrototype<S, U>{{MIN2(i1->_lo, i2->_lo), MAX2(i1->_hi, i2->_hi)},
                                         {MIN2(i1->_ulo, i2->_ulo), MAX2(i1->_uhi, i2->_uhi)},
                                         {i1->_bits._zeros & i2->_bits._zeros, i1->_bits._ones & i2->_bits._ones}},
                  MAX2(i1->_widen, i2->_widen), false);
    }
    // join
    return make(TypeIntPrototype<S, U>{{MAX2(i1->_lo, i2->_lo), MIN2(i1->_hi, i2->_hi)},
                                       {MAX2(i1->_ulo, i2->_ulo), MIN2(i1->_uhi, i2->_uhi)},
                                       {i1->_bits._zeros | i2->_bits._zeros, i1->_bits._ones | i2->_bits._ones}},
                MIN2(i1->_widen, i2->_widen), true);
  }

  assert(t2->base() != i1->base(), "");
  switch (t2->base()) {          // Switch on original type
  case Type::AnyPtr:                  // Mixing with oops happens when javac
  case Type::RawPtr:                  // reuses local variables
  case Type::OopPtr:
  case Type::InstPtr:
  case Type::AryPtr:
  case Type::MetadataPtr:
  case Type::KlassPtr:
  case Type::InstKlassPtr:
  case Type::AryKlassPtr:
  case Type::NarrowOop:
  case Type::NarrowKlass:
  case Type::Int:
  case Type::Long:
  case Type::FloatTop:
  case Type::FloatCon:
  case Type::FloatBot:
  case Type::DoubleTop:
  case Type::DoubleCon:
  case Type::DoubleBot:
  case Type::Bottom:                  // Ye Olde Default
    return Type::BOTTOM;
  default:                      // All else is a mistake
    i1->typerr(t2);
    return nullptr;
  }
}
template const Type* TypeIntHelper::int_type_xmeet(const TypeInt* i1, const Type* t2,
                                                   const Type* (*make)(const TypeIntPrototype<jint, juint>&, int, bool), bool dual);
template const Type* TypeIntHelper::int_type_xmeet(const TypeLong* i1, const Type* t2,
                                                   const Type* (*make)(const TypeIntPrototype<jlong, julong>&, int, bool), bool dual);

// Called in PhiNode::Value during CCP, monotically widen the value set, do so rigorously
// first, after WidenMax attempts, if the type has still not converged we speed up the
// convergence by abandoning the bounds
template <class CT>
const Type* TypeIntHelper::int_type_widen(const CT* new_type, const CT* old_type, const CT* limit_type) {
  using S = std::remove_const_t<decltype(CT::_lo)>;
  using U = std::remove_const_t<decltype(CT::_ulo)>;

  if (old_type == nullptr) {
    return new_type;
  }

  // If new guy is equal to old guy, no widening
  if (int_type_is_equal(new_type, old_type)) {
    return old_type;
  }

  // If old guy contains new, then we probably widened too far & dropped to
  // bottom. Return the wider fellow.
  if (int_type_is_subset(old_type, new_type)) {
    return old_type;
  }

  // Neither contains each other, weird?
  if (!int_type_is_subset(new_type, old_type)) {
    return CT::TYPE_DOMAIN;
  }

  // If old guy was a constant, do not bother
  if (old_type->singleton()) {
    return new_type;
  }

  // If new guy contains old, then we widened
  // If new guy is already wider than old, no widening
  if (new_type->_widen > old_type->_widen) {
    return new_type;
  }

  if (new_type->_widen < Type::WidenMax) {
    // Returned widened new guy
    TypeIntPrototype<S, U> prototype{{new_type->_lo, new_type->_hi}, {new_type->_ulo, new_type->_uhi}, new_type->_bits};
    return CT::try_make(prototype, new_type->_widen + 1);
  }

  // Speed up the convergence by abandoning the bounds, there are only a couple of bits so
  // they converge fast
  S min = std::numeric_limits<S>::min();
  S max = std::numeric_limits<S>::max();
  U umin = std::numeric_limits<U>::min();
  U umax = std::numeric_limits<U>::max();
  U zeros = new_type->_bits._zeros;
  U ones = new_type->_bits._ones;
  if (limit_type != nullptr) {
    min = limit_type->_lo;
    max = limit_type->_hi;
    umin = limit_type->_ulo;
    umax = limit_type->_uhi;
    zeros |= limit_type->_bits._zeros;
    ones |= limit_type->_bits._ones;
  }
  TypeIntPrototype<S, U> prototype{{min, max}, {umin, umax}, {zeros, ones}};
  return CT::try_make(prototype, Type::WidenMax);
}
template const Type* TypeIntHelper::int_type_widen(const TypeInt* new_type, const TypeInt* old_type, const TypeInt* limit_type);
template const Type* TypeIntHelper::int_type_widen(const TypeLong* new_type, const TypeLong* old_type, const TypeLong* limit_type);

// Called by PhiNode::Value during GVN, monotonically narrow the value set, only
// narrow if the bits change or if the bounds are tightened enough to avoid
// slow convergence
template <class CT>
const Type* TypeIntHelper::int_type_narrow(const CT* new_type, const CT* old_type) {
  using S = decltype(CT::_lo);
  using U = decltype(CT::_ulo);

  if (new_type->singleton() || old_type == nullptr) {
    return new_type;
  }

  // If new guy is equal to old guy, no narrowing
  if (int_type_is_equal(new_type, old_type)) {
    return old_type;
  }

  // If old guy was maximum range, allow the narrowing
  if (int_type_is_equal(old_type, CT::TYPE_DOMAIN)) {
    return new_type;
  }

  // Doesn't narrow; pretty weird
  if (!int_type_is_subset(old_type, new_type)) {
    return new_type;
  }

  // Bits change
  if (old_type->_bits._zeros != new_type->_bits._zeros || old_type->_bits._ones != new_type->_bits._ones) {
    return new_type;
  }

  // Only narrow if the range shrinks a lot
  U oc = cardinality_from_bounds(RangeInt<S>{old_type->_lo, old_type->_hi},
                                 RangeInt<U>{old_type->_ulo, old_type->_uhi});
  U nc = cardinality_from_bounds(RangeInt<S>{new_type->_lo, new_type->_hi},
                                 RangeInt<U>{new_type->_ulo, new_type->_uhi});
  return (nc > (oc >> 1) + (SMALL_TYPEINT_THRESHOLD * 2)) ? old_type : new_type;
}
template const Type* TypeIntHelper::int_type_narrow(const TypeInt* new_type, const TypeInt* old_type);
template const Type* TypeIntHelper::int_type_narrow(const TypeLong* new_type, const TypeLong* old_type);


#ifndef PRODUCT
template <class T>
static const char* int_name_near(T origin, const char* xname, char* buf, size_t buf_size, T n) {
  if (n < origin) {
    if (n <= origin - 10000) {
      return nullptr;
    }
    os::snprintf_checked(buf, buf_size, "%s-" INT32_FORMAT, xname, jint(origin - n));
  } else if (n > origin) {
    if (n >= origin + 10000) {
      return nullptr;
    }
    os::snprintf_checked(buf, buf_size, "%s+" INT32_FORMAT, xname, jint(n - origin));
  } else {
    return xname;
  }
  return buf;
}

const char* TypeIntHelper::intname(char* buf, size_t buf_size, jint n) {
  const char* str = int_name_near<jint>(max_jint, "maxint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<jint>(min_jint, "minint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  os::snprintf_checked(buf, buf_size, INT32_FORMAT, n);
  return buf;
}

const char* TypeIntHelper::uintname(char* buf, size_t buf_size, juint n) {
  const char* str = int_name_near<juint>(max_juint, "maxuint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<juint>(max_jint, "maxint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  os::snprintf_checked(buf, buf_size, UINT32_FORMAT"u", n);
  return buf;
}

const char* TypeIntHelper::longname(char* buf, size_t buf_size, jlong n) {
  const char* str = int_name_near<jlong>(max_jlong, "maxlong", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<jlong>(min_jlong, "minlong", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<jlong>(max_juint, "maxuint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<jlong>(max_jint, "maxint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<jlong>(min_jint, "minint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  os::snprintf_checked(buf, buf_size, JLONG_FORMAT, n);
  return buf;
}

const char* TypeIntHelper::ulongname(char* buf, size_t buf_size, julong n) {
  const char* str = int_name_near<julong>(max_julong, "maxulong", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<julong>(max_jlong, "maxlong", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<julong>(max_juint, "maxuint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  str = int_name_near<julong>(max_jint, "maxint", buf, buf_size, n);
  if (str != nullptr) {
    return str;
  }

  os::snprintf_checked(buf, buf_size, JULONG_FORMAT"u", n);
  return buf;
}

template <class U>
const char* TypeIntHelper::bitname(char* buf, size_t buf_size, U zeros, U ones) {
  constexpr juint W = sizeof(U) * 8;

  if (buf_size < W + 1) {
    return "#####";
  }

  for (juint i = 0; i < W; i++) {
    U mask = U(1) << (W - 1 - i);
    if ((zeros & mask) != 0) {
      buf[i] = '0';
    } else if ((ones & mask) != 0) {
      buf[i] = '1';
    } else {
      buf[i] = '*';
    }
  }
  buf[W] = 0;
  return buf;
}
template const char* TypeIntHelper::bitname(char* buf, size_t buf_size, juint zeros, juint ones);
template const char* TypeIntHelper::bitname(char* buf, size_t buf_size, julong zeros, julong ones);

void TypeIntHelper::int_type_dump(const TypeInt* t, outputStream* st, bool verbose) {
  char buf1[40], buf2[40], buf3[40], buf4[40], buf5[40];
  if (int_type_is_equal(t, TypeInt::INT)) {
    st->print("int");
  } else if (t->is_con()) {
    st->print("int:%s", intname(buf1, sizeof(buf1), t->get_con()));
  } else if (int_type_is_equal(t, TypeInt::BOOL)) {
    st->print("bool");
  } else if (int_type_is_equal(t, TypeInt::BYTE)) {
    st->print("byte");
  } else if (int_type_is_equal(t, TypeInt::CHAR)) {
    st->print("char");
  } else if (int_type_is_equal(t, TypeInt::SHORT)) {
    st->print("short");
  } else {
    if (verbose) {
      st->print("int:%s..%s, %s..%s, %s",
                intname(buf1, sizeof(buf1), t->_lo), intname(buf2, sizeof(buf2), t->_hi),
                uintname(buf3, sizeof(buf3), t->_ulo), uintname(buf4, sizeof(buf4), t->_uhi),
                bitname(buf5, sizeof(buf5), t->_bits._zeros, t->_bits._ones));
    } else {
      if (t->_lo >= 0) {
        if (t->_hi == max_jint) {
          st->print("int:>=%s", intname(buf1, sizeof(buf1), t->_lo));
        } else {
          st->print("int:%s..%s", intname(buf1, sizeof(buf1), t->_lo), intname(buf2, sizeof(buf2), t->_hi));
        }
      } else if (t->_hi < 0) {
        if (t->_lo == min_jint) {
          st->print("int:<=%s", intname(buf1, sizeof(buf1), t->_hi));
        } else {
          st->print("int:%s..%s", intname(buf1, sizeof(buf1), t->_lo), intname(buf2, sizeof(buf2), t->_hi));
        }
      } else {
        st->print("int:%s..%s, %s..%s",
                  intname(buf1, sizeof(buf1), t->_lo), intname(buf2, sizeof(buf2), t->_hi),
                  uintname(buf3, sizeof(buf3), t->_ulo), uintname(buf4, sizeof(buf4), t->_uhi));
      }

    }
  }

  if (t->_widen > 0 && t != TypeInt::INT) {
    st->print(", widen: %d", t->_widen);
  }
}

void TypeIntHelper::int_type_dump(const TypeLong* t, outputStream* st, bool verbose) {
  char buf1[80], buf2[80], buf3[80], buf4[80], buf5[80];
  if (int_type_is_equal(t, TypeLong::LONG)) {
    st->print("long");
  } else if (t->is_con()) {
    st->print("long:%s", longname(buf1, sizeof(buf1), t->get_con()));
  } else {
    if (verbose) {
      st->print("long:%s..%s ^ %s..%s, bits:%s",
                longname(buf1, sizeof(buf1), t->_lo), longname(buf2,sizeof(buf2), t-> _hi),
                ulongname(buf3, sizeof(buf3), t->_ulo), ulongname(buf4, sizeof(buf4), t->_uhi),
                bitname(buf5, sizeof(buf5), t->_bits._zeros, t->_bits._ones));
    } else {
      if (t->_lo >= 0) {
        if (t->_hi == max_jint) {
          st->print("long:>=%s", longname(buf1, sizeof(buf1), t->_lo));
        } else {
          st->print("long:%s..%s", longname(buf1, sizeof(buf1), t->_lo), longname(buf2, sizeof(buf2), t->_hi));
        }
      } else if (t->_hi < 0) {
        if (t->_lo == min_jint) {
          st->print("long:<=%s", longname(buf1, sizeof(buf1), t->_hi));
        } else {
          st->print("long:%s..%s", longname(buf1, sizeof(buf1), t->_lo), longname(buf2, sizeof(buf2), t->_hi));
        }
      } else {
        st->print("long:%s..%s ^ %s..%s",
                  longname(buf1, sizeof(buf1), t->_lo), longname(buf2,sizeof(buf2), t-> _hi),
                  ulongname(buf3, sizeof(buf3), t->_ulo), ulongname(buf4, sizeof(buf4), t->_uhi));
      }
    }
  }

  if (t->_widen > 0 && t != TypeLong::LONG) {
    st->print(", widen: %d", t->_widen);
  }
}
#endif // PRODUCT
