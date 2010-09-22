// Copyright (c) 2006 - 2008, Markus Strauch.
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
// THE POSSIBILITY OF SUCH DAMAGE.

package net.sf.sdedit.util;

/**
 * <tt>Pair</tt> objects can hold two objects of types <tt>F</tt> and <tt>S</tt>
 *
 * @param <F> the type of the first entry of the pair
 * @param <S> the type of the second entry of the pair
 * 
 */
public class Pair<F,S>
{
    private F first;
    
    private S second;
    
    /**
     * Creates a new <tt>Pair</tt>
     * 
     * @param first the first object
     * @param second the second object
     */
    public Pair (F first, S second)
    {
        this.first = first;
        this.second = second;
        
    }
    
    /**
     * Returns the first object
     * 
     * @return the first object
     */
    public F getFirst ()
    {
        return first;
    }
    
    /**
     * Returns the second object
     * 
     * @return the second object
     */
    public S getSecond ()
    {
        return second;
    }
    
    /**
     * Sets the first object.
     * 
     * @param first the first object of the pair
     */
    public void setFirst (F first)
    {
        this.first = first;
    }
    
    /**
     * Sets the second object.
     * 
     * @param second the second object of the pair
     */
    public void setSecond (S second)
    {
        this.second = second;
    }
    
    /**
     * Returns this Pair's hash code, of which the 16 high bits are the
     * 16 low bits of <tt>getFirst().hashCode()</tt> are identical to 
     * the 16 low bits and the 16 low bits of <tt>getSecond().hashCode()</tt>
     * 
     * @return this Pair's hash code, composed of its two elements' hash codes
     * as described above
     */
    public int hashCode ()
    {
        return (first.hashCode() << 16) | second.hashCode() & 0xFFFF;
    }
    
    /**
     * Returns true iff both elements of this Pair equal both elements of
     * another Pair.
     * 
     * @param object object reference to another Pair
     * @return true iff both elements of this Pair equal both elements of
     * another Pair
     */
    @SuppressWarnings("unchecked")
    public boolean equals (Object object)
    {
        Pair<F,S> pair = (Pair<F,S>) object;
        return first.equals(pair.first) && second.equals(pair.second);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString ()
    {
        return "<" + getFirst() + "," + getSecond() + ">";
    }

}
