package me.pesekjak.hippo.classes;

public abstract class IType {

    protected String descriptor;
    protected boolean isVarArg = false;

    public abstract String getDescriptor();

    public abstract String getRawDescriptor();

    public abstract IType arrayType();

    public abstract IType varArgType();

    protected String arrayBlocks() {
        StringBuilder builder = new StringBuilder();
        String input = descriptor;
        int x;
        while ((x = input.indexOf("[")) > -1) {
            input = input.substring(x + 1);
            builder.append("[]");
        }
        return builder.toString();
    }

    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    public boolean isVarArg() {
        return isVarArg;
    }

    public void setVarArg(boolean varArg) {
        isVarArg = varArg;
    }

}
