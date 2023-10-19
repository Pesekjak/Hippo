/**
 * Handles loading and reloading for compiled custom classes.
 * <p>
 * Compilation process:
 * <p>
 * When a structure of a custom class is loaded ({@link me.pesekjak.hippo.elements.structures.StructNewClass#load()}),
 * new class signature of the class is created and added to the next class update.
 * <p>
 * Once parser finished parsing all scripts ({@link me.pesekjak.hippo.skript.ScriptInactiveEvent})
 * the next class update is enrolled.
 * <p>
 * All unchanged classes (classes where their bytecode has not changed, does not include the user's Skript code) are filtered,
 * remaining classes are checked for illegal hierarchy and new {@link me.pesekjak.hippo.core.loader.SingleClassLoader}s are
 * created for those which pass the checks. Illegal classes and their children are removed from the dynamic class loader.
 * <p>
 * Old single class loaders are then removed from the {@link me.pesekjak.hippo.core.loader.DynamicClassLoader} and replaced
 * with newly created ones.
 * <p>
 * Hippo only reloads classes that need to be reloaded due to bytecode changes.
 */
package me.pesekjak.hippo.core.loader;
