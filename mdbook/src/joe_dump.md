# 'joe dump'

The `joe dump` tool dumps debugging information about Joe scripts to 
standard output.  It is primarily intended for use by the 
Joe developer/maintainer.

**Note:** the format and content of the data dumped by this tool is *not* 
part of Joe's public interface and will change without notice with
changes to the parser, virtual machine, and the needs of the maintainer.

## Dumping Byte-Code

By default, it compiles the script to byte-code and dumps a disassembly
listing of the byte-code.  See the [Virtual Machine](vm.md) appendix for
the current byte-code instructions. 

```text
$ joe dump hello.joe
Byte-code: hello.joe

=== SCRIPT *script* ===
Lines RLE size: 2/9 = 22.2%
[0] = 'println'
[1] = 'Hello, world!'

0001                                      println("Hello, world!");
0001  @0000 GLOGET  0000 'println'        println("Hello, world!");
   |  @0002 CONST   0001 'Hello, ...'   
   |  @0004 CALL    0001                
   |  @0006 RETURN                      
   |  @0007 NULL                        
   |  @0008 RETURN                      
```

## Dumping the Abstract Syntax Tree

Given the `--ast` option, `joe dump` outputs the script's abstract syntax tree:

```text
$ joe dump --ast hello.joe
AST: hello.joe

Stmt.Expression
  Expr.Call
    callee: Expr.VarGet 'println'
    arg: Expr.Literal 'Hello, world!'
```



