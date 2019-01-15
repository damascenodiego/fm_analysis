/* Generated By:JJTree&JavaCC: Do not edit this line. FCONSTRAINT.java */
package br.usp.icmc.parser;

public class FCONSTRAINT/*@bgen(jjtree)*/implements FCONSTRAINTTreeConstants, FCONSTRAINTConstants {/*@bgen(jjtree)*/
  protected static JJTFCONSTRAINTState jjtree = new JJTFCONSTRAINTState();public static void main(String args []) throws ParseException  {
    new FCONSTRAINT(System.in);
    while (true) {
      System.out.println("Reading from standard input...");
      try {
        SimpleNode root = FCONSTRAINT.Parse();
        if(root == null) {
                        System.out.println("Goodbye.");
        }else {
            root.dump("-");
            //System.out.println(toString(prefix) + "(" + jjtGetValue()+ ")");        }
        return;
      }
      catch (Exception e) {
        System.out.println("Erro sint\u00e1tico.");
        System.out.println(e.getMessage());
        FCONSTRAINT.ReInit(System.in);
      }
      catch (Error e) {
        System.out.println("Erro l\u00e9xico.");
        System.out.println(e.getMessage());
        break;
      }
    }
  }

  static final public SimpleNode Parse() throws ParseException {
                     /*@bgen(jjtree) Parse */
  SimpleNode jjtn000 = new SimpleNode(JJTPARSE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NEG:
      case ID:
      case 11:
        term();
                                  jjtree.closeNodeScope(jjtn000, true);
                                  jjtc000 = false;
                         {if (true) return jjtn000;}
        break;
      case 10:
        jj_consume_token(10);
                                  jjtree.closeNodeScope(jjtn000, true);
                                  jjtc000 = false;
                         {if (true) return null;}
        break;
      default:
        jj_la1[0] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object term() throws ParseException {
                                                 /*@bgen(jjtree) term */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTTERM);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Object i,a="",b="",c="";String z="";
    try {
      i = unary();
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
        case OR:
          ;
          break;
        default:
          jj_la1[1] = jj_gen;
          break label_1;
        }
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
          a = AND();
          break;
        case OR:
          b = OR();
          break;
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        c = unary();
      }
                                                 z=z.concat(""+i+a+b+c);
    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
   jjtn000.jjtSetValue(z);      {if (true) return z;}
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object unary() throws ParseException {
                                                 /*@bgen(jjtree) unary */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTUNARY);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Object i,a,b;String z="";
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ID:
      case 11:
        i = element();
                                                 z=z.concat(i+"");
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
         jjtn000.jjtSetValue(z);        {if (true) return z;}
        break;
      case NEG:
        a = NEG();
        b = element();
                                         z=z.concat(a+""+b);
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
         jjtn000.jjtSetValue(z);        {if (true) return z;}
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object element() throws ParseException {
                                                 /*@bgen(jjtree) element */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTELEMENT);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Object i,t;String z="";
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ID:
        i = ID();
                                                         z=z.concat(i+"");
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
         jjtn000.jjtSetValue(z);        {if (true) return z;}
        break;
      case 11:
        jj_consume_token(11);
        t = term();
        jj_consume_token(12);
                                                 z=z.concat("("+t+")");
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
         jjtn000.jjtSetValue(z);        {if (true) return z;}
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object NEG() throws ParseException {
                                                 /*@bgen(jjtree) neg */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTNEG);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(NEG);
             jjtree.closeNodeScope(jjtn000, true);
             jjtc000 = false;
            jjtn000.value = t.image; {if (true) return t.image;}
    } finally {
     if (jjtc000) {
       jjtree.closeNodeScope(jjtn000, true);
     }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object AND() throws ParseException {
                                                 /*@bgen(jjtree) and */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTAND);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(AND);
             jjtree.closeNodeScope(jjtn000, true);
             jjtc000 = false;
            jjtn000.value = t.image; {if (true) return t.image;}
    } finally {
     if (jjtc000) {
       jjtree.closeNodeScope(jjtn000, true);
     }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object OR() throws ParseException {
                                                 /*@bgen(jjtree) or */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTOR);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(OR);
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
           jjtn000.value = t.image; {if (true) return t.image;}
    } finally {
     if (jjtc000) {
       jjtree.closeNodeScope(jjtn000, true);
     }
    }
    throw new Error("Missing return statement in function");
  }

  static final public Object ID() throws ParseException {
                                                 /*@bgen(jjtree) id */
                                                 SimpleNode jjtn000 = new SimpleNode(JJTID);
                                                 boolean jjtc000 = true;
                                                 jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(ID);
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
           jjtn000.value = t.image; {if (true) return t.image;}
    } finally {
     if (jjtc000) {
       jjtree.closeNodeScope(jjtn000, true);
     }
    }
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public FCONSTRAINTTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[5];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0xd20,0xc0,0xc0,0x920,0x900,};
   }

  /** Constructor with InputStream. */
  public FCONSTRAINT(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public FCONSTRAINT(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new FCONSTRAINTTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public FCONSTRAINT(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FCONSTRAINTTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public FCONSTRAINT(FCONSTRAINTTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(FCONSTRAINTTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  static private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[13];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 5; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 13; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

}
