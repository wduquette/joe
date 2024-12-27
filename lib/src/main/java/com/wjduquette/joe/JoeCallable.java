package com.wjduquette.joe;

/**
 * JoeCallable is a marker interface for objects that can be called as
 * functions by an Engine. It defines the methods that all callables
 * must provide to Joe client.  The client can call a JoeCallable directly
 * via the Joe::call method.</p>
 *
 * <p><b>Note:</b> A scripted callable can only be called using the engine
 * in which it was defined.  In particular, different engines implement scripted
 * functions and methods quite differently; thus, a scripted callable from one
 * engine can never be used with a different engine.</p>
 */
public interface JoeCallable {
}
