package me.foly.microcalc;

import me.foly.microcalc.models.*;
import org.jparsec.*;

public class ParseEngine {
  private final CalcClient calcClient;
  private final Terminals OPERATORS = Terminals.operators("+", "-", "*", "/", "(", ")", "^", "%");
  private final Parser<?> WHITESPACE_MUL = term("+", "-", "*", "/", "^", "%").not();
  private final Parser<?> TOKENIZER =
      Parsers.or(Terminals.IntegerLiteral.TOKENIZER, OPERATORS.tokenizer());
  private final Parser<Void> IGNORED =
      Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES)
          .skipMany();
  private final Parser<Node> NUMBER =
      Terminals.IntegerLiteral.PARSER.map(l -> new NodeVal(Integer.valueOf(l)));
  public Parser<Node> CALCULATOR = calculator(NUMBER).from(TOKENIZER, IGNORED);

  public ParseEngine(CalcClient calcClient) {
    this.calcClient = calcClient;
  }

  private NodeBin bin(NodeType type, Node l, Node r) {
    return new NodeBin(type, l, r, calcClient);
  }

  private NodeUn un(NodeType type, Node v) {
    return new NodeUn(type, v, calcClient);
  }

  private Parser<?> term(String... names) {
    return OPERATORS.token(names);
  }

  private <T> Parser<T> op(String name, T value) {
    return term(name).retn(value);
  }

  private Parser<Node> calculator(Parser<Node> atom) {
    Parser.Reference<Node> ref = Parser.newReference();
    Parser<Node> unit = ref.lazy().between(term("("), term(")")).or(atom);
    Parser<Node> parser =
        new OperatorTable<Node>()
            .infixl(op("+", (l, r) -> bin(NodeType.OP_PLUS, l, r)), 10)
            .infixl(op("-", (l, r) -> bin(NodeType.OP_MINUS, l, r)), 10)
            .infixl(
                Parsers.or(term("*"), WHITESPACE_MUL).retn((l, r) -> bin(NodeType.OP_MULT, l, r)),
                20)
            .infixl(op("/", (l, r) -> bin(NodeType.OP_DIV, l, r)), 20)
            .infixl(op("%", (l, r) -> bin(NodeType.OP_MOD, l, r)), 20)
            .infixl(op("^", (l, r) -> bin(NodeType.OP_POW, l, r)), 40)
            .prefix(op("-", v -> un(NodeType.OP_NEG, v)), 30)
            .build(unit);
    ref.set(parser);
    return parser;
  }
}
