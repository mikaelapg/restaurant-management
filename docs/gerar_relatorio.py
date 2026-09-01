#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gera o Relatorio Tecnico do Tech Challenge (Fase 1) como arquivo .docx
formatado segundo a ABNT NBR 14724 (trabalhos academicos), usando somente a
biblioteca padrao do Python (zipfile + OOXML escrito a mao).

    python3 docs/gerar_relatorio.py           ->  docs/RELATORIO-TECH-CHALLENGE.docx

Formatacao aplicada:
  - Papel A4; margens 3 cm (esq./sup.) e 2 cm (dir./inf.);
  - Fonte Arial 12 no texto; 10 nas legendas, fontes e citacoes;
  - Espacamento entre linhas 1,5; recuo de primeira linha de 1,25 cm;
  - Estrutura: capa, folha de rosto, resumo, abstract, lista de ilustracoes,
    lista de quadros, sumario, introducao, desenvolvimento, consideracoes
    finais e referencias;
  - Numeracao progressiva das secoes (NBR 6024);
  - Cada figura e antecedida de um paragrafo que a explica; a legenda e a
    fonte seguem o padrao NBR 14724 / IBGE;
  - Paginacao no canto superior direito a partir da parte textual;
  - Sumario, lista de ilustracoes e lista de quadros como campos automaticos
    do Word -> ao abrir, selecione tudo (Ctrl+T) e tecle F9 para atualizar.

Cada figura tem um espaco delimitado com a instrucao "Inserir aqui a imagem".
Abra no Word, clique nessa linha, use Inserir > Imagem e apague o texto de
instrucao. A Figura 1 e o diagrama de classes do modelo de dominio, gerado com
PlantUML/Mermaid a partir de docs/domain-model.puml (ou .mmd); as demais
correspondem, na ordem, as capturas de tela da autora.

TODAS as legendas de figura sao campos SEQ automaticos (funcao _legenda), o que
as faz aparecer na "Lista de Ilustracoes" (TOC \\c "Figura") apos F9. Nao digite
legendas manualmente no Word: gere o docx por este script e apenas insira as
imagens.
"""

import os
import zipfile
from datetime import datetime, timezone
from xml.sax.saxutils import escape

DOCX_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                         "RELATORIO-TECH-CHALLENGE.docx")
ANO = "2026"
DATA_ACESSO = "30 ago. 2026"

REPO_URL = "https://github.com/mikaelapg/restaurant-management"
AUTORA = "MIKAELA PEREZ GUEDES"

NS = ('xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" '
      'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"')

_body = []
_fig = 0        # contador de figuras
_quad = 0       # contador de quadros


def _esc(t):
    return escape(str(t))


def _rpr(bold=False, italic=False, mono=False, sz=None, caps=False, color=None):
    x = []
    if mono:
        x.append('<w:rFonts w:ascii="Courier New" w:hAnsi="Courier New" w:cs="Courier New"/>')
    if bold:
        x.append('<w:b/>')
    if italic:
        x.append('<w:i/>')
    if caps:
        x.append('<w:caps/>')
    if color:
        x.append('<w:color w:val="%s"/>' % color)
    if sz:
        x.append('<w:sz w:val="%d"/><w:szCs w:val="%d"/>' % (sz, sz))
    return '<w:rPr>%s</w:rPr>' % ''.join(x) if x else ''


def _runs(text, **kw):
    rpr = _rpr(**kw)
    parts = _esc(text).split('\n')
    out = []
    for i, seg in enumerate(parts):
        if i > 0:
            out.append('<w:r>%s<w:br/></w:r>' % rpr)
        out.append('<w:r>%s<w:t xml:space="preserve">%s</w:t></w:r>' % (rpr, seg))
    return ''.join(out)


def p(text="", style=None, bold=False, italic=False, mono=False, sz=None,
      jc=None, ind_first=None, spacing_after=None, spacing_line=None, keep_next=False):
    ppr = []
    if style:
        ppr.append('<w:pStyle w:val="%s"/>' % style)
    if keep_next:
        ppr.append('<w:keepNext/>')
    sp = []
    if spacing_after is not None:
        sp.append('w:after="%d"' % spacing_after)
    if spacing_line is not None:
        sp.append('w:line="%d" w:lineRule="auto"' % spacing_line)
    if sp:
        ppr.append('<w:spacing %s/>' % ' '.join(sp))
    if ind_first is not None:
        ppr.append('<w:ind w:firstLine="%d"/>' % ind_first)
    if jc:
        ppr.append('<w:jc w:val="%s"/>' % jc)
    ppr_xml = '<w:pPr>%s</w:pPr>' % ''.join(ppr) if ppr else ''
    _body.append('<w:p>%s%s</w:p>' % (ppr_xml, _runs(text, bold=bold, italic=italic,
                                                     mono=mono, sz=sz)))


def page_break():
    _body.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')


def h1(text):
    _body.append('<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr>%s</w:p>'
                 % _runs(text))


def h1_sem_numero(text):
    # Titulo de secao pos-textual: entra no sumario, sem indicativo numerico, centralizado.
    _body.append('<w:p><w:pPr><w:pStyle w:val="Heading1"/><w:jc w:val="center"/></w:pPr>%s</w:p>'
                 % _runs(text))


def h2(text):
    _body.append('<w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr>%s</w:p>' % _runs(text))


def h3(text):
    _body.append('<w:p><w:pPr><w:pStyle w:val="Heading3"/></w:pPr>%s</w:p>' % _runs(text))


def titulo_pre(text):
    _body.append('<w:p><w:pPr><w:pStyle w:val="TituloPreTextual"/></w:pPr>%s</w:p>'
                 % _runs(text))


def bullet(text, prefix_bold=None):
    inner = _runs(prefix_bold, bold=True) if prefix_bold else ''
    inner += _runs(text)
    _body.append('<w:p><w:pPr><w:pStyle w:val="ListBullet"/>'
                 '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>%s</w:p>'
                 % inner)


def code_block(text):
    lines = text.strip('\n').split('\n')
    n = len(lines)
    for i, line in enumerate(lines):
        top = '<w:top w:val="single" w:sz="6" w:space="4" w:color="C0C0C0"/>' if i == 0 else ''
        bot = '<w:bottom w:val="single" w:sz="6" w:space="4" w:color="C0C0C0"/>' if i == n - 1 else ''
        _body.append(
            '<w:p><w:pPr><w:pBdr>%s'
            '<w:left w:val="single" w:sz="6" w:space="4" w:color="C0C0C0"/>%s'
            '<w:right w:val="single" w:sz="6" w:space="4" w:color="C0C0C0"/></w:pBdr>'
            '<w:shd w:val="clear" w:color="auto" w:fill="F4F4F4"/>'
            '<w:spacing w:before="%d" w:after="%d" w:line="240" w:lineRule="auto"/>'
            '<w:ind w:firstLine="0"/></w:pPr>%s</w:p>'
            % (top, bot, 40 if i == 0 else 0, 40 if i == n - 1 else 0,
               _runs(line or ' ', mono=True, sz=18)))


def _seq_field(name, cached):
    rpr = _rpr(sz=20)
    return ('<w:r>%s<w:fldChar w:fldCharType="begin"/></w:r>'
            '<w:r>%s<w:instrText xml:space="preserve"> SEQ %s \\* ARABIC </w:instrText></w:r>'
            '<w:r>%s<w:fldChar w:fldCharType="separate"/></w:r>'
            '<w:r>%s<w:t>%d</w:t></w:r>'
            '<w:r>%s<w:fldChar w:fldCharType="end"/></w:r>'
            % (rpr, rpr, name, rpr, rpr, cached, rpr))


def _legenda(prefixo, name, cached, titulo):
    _body.append(
        '<w:p><w:pPr><w:pStyle w:val="Legenda"/><w:keepNext/><w:keepLines/></w:pPr>'
        '%s%s%s</w:p>'
        % (_runs('%s ' % prefixo, sz=20),
           _seq_field(name, cached),
           _runs(' \u2013 ' + titulo, sz=20)))


def _fonte(txt):
    p(txt, style="Fonte", sz=20)


def _placeholder_imagem():
    _body.append(
        '<w:p><w:pPr>'
        '<w:pBdr>'
        '<w:top w:val="single" w:sz="4" w:space="8" w:color="BFBFBF"/>'
        '<w:left w:val="single" w:sz="4" w:space="8" w:color="BFBFBF"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="8" w:color="BFBFBF"/>'
        '<w:right w:val="single" w:sz="4" w:space="8" w:color="BFBFBF"/>'
        '</w:pBdr>'
        '<w:shd w:val="clear" w:color="auto" w:fill="FAFAFA"/>'
        '<w:spacing w:before="60" w:after="60" w:line="240" w:lineRule="auto"/>'
        '<w:ind w:firstLine="0"/><w:jc w:val="center"/>'
        '<w:keepNext/><w:keepLines/>'
        '</w:pPr>%s</w:p>'
        % _runs('\nInserir aqui a imagem \u2014 apague esta linha depois de inseri-la.\n',
                italic=True, sz=18, color="A6A6A6"))


def figura(titulo, intro=None, obs=None):
    """intro: paragrafo de contexto ANTES da figura. obs: comentario logo APOS."""
    global _fig
    if intro:
        p(intro)
    _fig += 1
    _legenda("Figura", "Figura", _fig, titulo)
    _placeholder_imagem()
    _fonte("Fonte: captura de tela da autora (%s)." % ANO)
    if obs:
        p(obs)


def figura_texto(titulo, texto_mono, intro=None,
                 fonte="Fonte: elaborado pela autora (%s)."):
    global _fig
    if intro:
        p(intro)
    _fig += 1
    _legenda("Figura", "Figura", _fig, titulo)
    code_block(texto_mono)
    _fonte(fonte % ANO if "%s" in fonte else fonte)


def figura_arquivo(titulo, intro=None, fonte=None, obs=None):
    """Figura cuja imagem vem de um arquivo elaborado pela autora (diagrama),
    e nao de uma captura de tela. Legenda em campo SEQ automatico."""
    global _fig
    if intro:
        p(intro)
    _fig += 1
    _legenda("Figura", "Figura", _fig, titulo)
    _placeholder_imagem()
    _fonte(fonte or ("Fonte: elaborado pela autora (%s)." % ANO))
    if obs:
        p(obs)


def quadro(titulo, headers, rows, widths, fonte=None):
    global _quad
    _quad += 1
    _legenda("Quadro", "Quadro", _quad, titulo)

    grid = ''.join('<w:gridCol w:w="%d"/>' % w for w in widths)

    def cell(text, w, header=False):
        return ('<w:tc><w:tcPr><w:tcW w:w="%d" w:type="dxa"/>%s'
                '<w:tcMar><w:top w:w="40" w:type="dxa"/><w:left w:w="80" w:type="dxa"/>'
                '<w:bottom w:w="40" w:type="dxa"/><w:right w:w="80" w:type="dxa"/></w:tcMar>'
                '</w:tcPr>'
                '<w:p><w:pPr><w:spacing w:before="20" w:after="20" w:line="240" '
                'w:lineRule="auto"/><w:ind w:firstLine="0"/></w:pPr>%s</w:p></w:tc>'
                % (w,
                   '<w:shd w:val="clear" w:color="auto" w:fill="E6E6E6"/>' if header else '',
                   _runs(text, bold=header, sz=20)))

    trs = ['<w:tr><w:trPr><w:tblHeader/></w:trPr>'
           + ''.join(cell(hh, widths[i], True) for i, hh in enumerate(headers)) + '</w:tr>']
    for row in rows:
        trs.append('<w:tr>' + ''.join(cell(c, widths[i]) for i, c in enumerate(row)) + '</w:tr>')

    _body.append(
        '<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/>'
        '<w:tblBorders>'
        '<w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '</w:tblBorders>'
        '<w:tblLook w:val="04A0" w:firstRow="1" w:lastRow="0" w:firstColumn="1" '
        'w:lastColumn="0" w:noHBand="0" w:noVBand="1"/></w:tblPr>'
        '<w:tblGrid>%s</w:tblGrid>%s</w:tbl>' % (grid, ''.join(trs)))
    _body.append('<w:p><w:pPr><w:spacing w:after="0"/><w:ind w:firstLine="0"/></w:pPr></w:p>')
    _fonte(fonte or ("Fonte: elaborado pela autora (%s)." % ANO))


def campo_toc(instr, dica):
    _body.append(
        '<w:p><w:pPr><w:ind w:firstLine="0"/></w:pPr>'
        '<w:r><w:fldChar w:fldCharType="begin"/></w:r>'
        '<w:r><w:instrText xml:space="preserve">%s</w:instrText></w:r>'
        '<w:r><w:fldChar w:fldCharType="separate"/></w:r>'
        '%s'
        '<w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>'
        % (_esc(instr), _runs(dica, italic=True, sz=20, color="808080")))


# --------------------------------------------------------------------------- #
# Conteudo                                                                     #
# --------------------------------------------------------------------------- #

def build_body():
    # ===================== CAPA =====================
    for _ in range(1):
        p("", spacing_after=0)
    p("FIAP \u2013 FACULDADE DE INFORM\u00c1TICA E ADMINISTRA\u00c7\u00c3O PAULISTA",
      jc="center", bold=True, ind_first=0, spacing_after=0)
    p("P\u00d3S-GRADUA\u00c7\u00c3O \u2013 ARQUITETURA E DESENVOLVIMENTO JAVA (POSTECH)",
      jc="center", bold=True, ind_first=0, spacing_after=0)
    for _ in range(6):
        p("", spacing_after=0)
    p(AUTORA, jc="center", ind_first=0, spacing_after=0)
    for _ in range(6):
        p("", spacing_after=0)
    p("RELAT\u00d3RIO T\u00c9CNICO \u2013 TECH CHALLENGE FASE 1", jc="center",
      bold=True, sz=28, ind_first=0, spacing_after=0)
    p("Sistema compartilhado de gest\u00e3o de restaurantes: API REST para "
      "cadastro e autentica\u00e7\u00e3o de usu\u00e1rios", jc="center", ind_first=0,
      spacing_after=0)
    for _ in range(12):
        p("", spacing_after=0)
    p("S\u00e3o Paulo", jc="center", ind_first=0, spacing_after=0)
    p(ANO, jc="center", ind_first=0, spacing_after=0)

    # ===================== FOLHA DE ROSTO =====================
    page_break()
    for _ in range(1):
        p("", spacing_after=0)
    p(AUTORA, jc="center", ind_first=0, spacing_after=0)
    for _ in range(6):
        p("", spacing_after=0)
    p("RELAT\u00d3RIO T\u00c9CNICO \u2013 TECH CHALLENGE FASE 1", jc="center",
      bold=True, sz=28, ind_first=0, spacing_after=0)
    p("Sistema compartilhado de gest\u00e3o de restaurantes: API REST para "
      "cadastro e autentica\u00e7\u00e3o de usu\u00e1rios", jc="center", ind_first=0,
      spacing_after=0)
    for _ in range(4):
        p("", spacing_after=0)
    p("Relat\u00f3rio t\u00e9cnico apresentado como requisito de avalia\u00e7\u00e3o "
      "da Fase 1 do Tech Challenge do curso de p\u00f3s-gradua\u00e7\u00e3o em "
      "Arquitetura e Desenvolvimento Java (POSTECH) da FIAP.",
      style="Natureza")
    p("Orientador(a): [nome do(a) professor(a) orientador(a)].", style="Natureza")
    for _ in range(8):
        p("", spacing_after=0)
    p("S\u00e3o Paulo", jc="center", ind_first=0, spacing_after=0)
    p(ANO, jc="center", ind_first=0, spacing_after=0)

    # ===================== RESUMO =====================
    titulo_pre("RESUMO")
    p("Este relat\u00f3rio t\u00e9cnico descreve o desenvolvimento de uma interface "
      "de programa\u00e7\u00e3o de aplica\u00e7\u00f5es (API) REST para o sistema "
      "compartilhado de gest\u00e3o de restaurantes proposto na Fase 1 do Tech "
      "Challenge da p\u00f3s-gradua\u00e7\u00e3o em Arquitetura e Desenvolvimento "
      "Java. O objetivo foi construir um backend que realize o cadastro, a "
      "consulta, a atualiza\u00e7\u00e3o e a exclus\u00e3o dos dois perfis de "
      "usu\u00e1rio do dom\u00ednio \u2014 cliente e dono de restaurante \u2014, com "
      "valida\u00e7\u00e3o de credenciais, busca por nome, registro da data da "
      "\u00faltima altera\u00e7\u00e3o e troca de senha em endpoint exclusivo, "
      "separado do endpoint de atualiza\u00e7\u00e3o cadastral. A solu\u00e7\u00e3o "
      "foi implementada em Java 21 com o framework Spring Boot 3.3.5, utilizando "
      "Spring Data JPA para persist\u00eancia, PostgreSQL 16 como banco relacional "
      "e Flyway para o versionamento do esquema. A heran\u00e7a entre os perfis "
      "adota a estrat\u00e9gia JOINED; o versionamento da API \u00e9 feito pela "
      "URI; e as respostas de erro seguem o padr\u00e3o Problem Details (RFC 7807). "
      "A documenta\u00e7\u00e3o \u00e9 publicada com Swagger/OpenAPI e os "
      "cen\u00e1rios de uso s\u00e3o cobertos por uma cole\u00e7\u00e3o Postman, por "
      "70 testes unit\u00e1rios com JUnit 5 e Mockito e por 6 testes de integra\u00e7\u00e3o "
      "com Testcontainers contra um PostgreSQL real, com cobertura medida por "
      "JaCoCo. A aplica\u00e7\u00e3o e o "
      "banco s\u00e3o orquestrados com Docker Compose. Os resultados obtidos "
      "atendem integralmente aos requisitos funcionais e aos crit\u00e9rios de "
      "avalia\u00e7\u00e3o estabelecidos no enunciado.",
      style="Resumo")
    p("Palavras-chave: API REST; Spring Boot; PostgreSQL; SOLID; RFC 7807; Docker.",
      style="Resumo", spacing_after=0)

    # ===================== ABSTRACT =====================
    titulo_pre("ABSTRACT")
    p("This technical report describes the development of a REST application "
      "programming interface (API) for the shared restaurant management system "
      "proposed in Phase 1 of the Tech Challenge of the graduate program in Java "
      "Architecture and Development. The goal was to build a backend that "
      "performs the creation, retrieval, update and deletion of the two user "
      "profiles of the domain \u2014 customer and restaurant owner \u2014 with "
      "credential validation, search by name, recording of the last modification "
      "date and password change through a dedicated endpoint, separated from the "
      "profile update endpoint. The solution was implemented in Java 21 with the "
      "Spring Boot 3.3.5 framework, using Spring Data JPA for persistence, "
      "PostgreSQL 16 as the relational database and Flyway for schema "
      "versioning. Inheritance between profiles uses the JOINED strategy; API "
      "versioning is done through the URI; and error responses follow the "
      "Problem Details standard (RFC 7807). Documentation is published with "
      "Swagger/OpenAPI and the usage scenarios are covered by a Postman "
      "collection, by 70 unit tests with JUnit 5 and Mockito and by 6 "
      "integration tests with Testcontainers against a real PostgreSQL, with "
      "coverage measured by JaCoCo. The "
      "application and the database are orchestrated with Docker Compose. The "
      "results fully meet the functional requirements and the evaluation "
      "criteria established in the assignment.",
      style="Resumo")
    p("Keywords: REST API; Spring Boot; PostgreSQL; SOLID; RFC 7807; Docker.",
      style="Resumo", spacing_after=0)

    # ===================== LISTA DE ILUSTRACOES =====================
    titulo_pre("LISTA DE ILUSTRA\u00c7\u00d5ES")
    campo_toc(' TOC \\h \\z \\c "Figura" ',
              "Lista autom\u00e1tica de figuras. No Word, selecione tudo (Ctrl+T) e "
              "tecle F9 para preencher com os t\u00edtulos e as p\u00e1ginas.")

    # ===================== LISTA DE QUADROS =====================
    titulo_pre("LISTA DE QUADROS")
    campo_toc(' TOC \\h \\z \\c "Quadro" ',
              "Lista autom\u00e1tica de quadros. No Word, selecione tudo (Ctrl+T) e "
              "tecle F9 para atualizar.")

    # ===================== SUMARIO =====================
    titulo_pre("SUM\u00c1RIO")
    campo_toc(' TOC \\o "1-3" \\h \\z \\u ',
              "Sum\u00e1rio autom\u00e1tico. No Word, selecione tudo (Ctrl+T) e tecle "
              "F9 para gerar os t\u00edtulos e as p\u00e1ginas.")

    # ---- fim da parte pre-textual: quebra de secao (sem numeracao de pagina) ----
    _body.append('<w:p><w:pPr><w:sectPr>'
                 '<w:pgSz w:w="11906" w:h="16838"/>'
                 '<w:pgMar w:top="1701" w:right="1134" w:bottom="1134" w:left="1701" '
                 'w:header="1134" w:footer="1134" w:gutter="0"/>'
                 '<w:type w:val="nextPage"/>'
                 '</w:sectPr></w:pPr></w:p>')

    # ===================== 1 INTRODUCAO =====================
    h1("1 INTRODU\u00c7\u00c3O")
    p("Um grupo de restaurantes da regi\u00e3o decidiu contratar estudantes para "
      "construir, de forma colaborativa, um \u00fanico sistema de gest\u00e3o "
      "compartilhado, reduzindo o custo de manter solu\u00e7\u00f5es individuais. "
      "A entrega \u00e9 dividida em fases; esta Fase 1 concentra-se no backend de "
      "gest\u00e3o de usu\u00e1rios.")
    p("O objetivo deste trabalho foi desenvolver um backend completo e robusto "
      "com Spring Boot, aplicando os princ\u00edpios estudados na fase, que "
      "permita o cadastro, a atualiza\u00e7\u00e3o e a exclus\u00e3o de "
      "usu\u00e1rios; a troca de senha em endpoint separado do de "
      "atualiza\u00e7\u00e3o cadastral; o registro da data da \u00faltima "
      "altera\u00e7\u00e3o; a busca de usu\u00e1rios pelo nome; a garantia de "
      "unicidade do e-mail; e um servi\u00e7o de valida\u00e7\u00e3o de login. A "
      "aplica\u00e7\u00e3o \u00e9 dockerizada e orquestrada com Docker Compose "
      "junto a um banco de dados relacional.")
    p("O escopo contempla obrigatoriamente dois perfis de usu\u00e1rio \u2014 "
      "dono de restaurante e cliente \u2014, ambos com nome, e-mail (\u00fanico), "
      "login, senha, data da \u00faltima altera\u00e7\u00e3o e endere\u00e7o. "
      "Funcionalidades de avalia\u00e7\u00f5es e de pedidos on-line citadas na "
      "vis\u00e3o geral do produto pertencem a fases posteriores e n\u00e3o "
      "integram esta entrega.")
    p("O c\u00f3digo-fonte completo \u2014 incluindo este relat\u00f3rio em formato edit\u00e1vel, "
      "o README, a especifica\u00e7\u00e3o Swagger/OpenAPI e a cole\u00e7\u00e3o Postman em JSON \u2014 "
      "est\u00e1 no reposit\u00f3rio aberto em " + REPO_URL + ". A modelagem das camadas, "
      "a ader\u00eancia aos princ\u00edpios SOLID e a rastreabilidade do projeto s\u00e3o "
      "detalhadas na se\u00e7\u00e3o 2.")
    p("Este relat\u00f3rio est\u00e1 organizado da seguinte forma: a se\u00e7\u00e3o "
      "2 apresenta a arquitetura da aplica\u00e7\u00e3o e as decis\u00f5es de "
      "projeto; a se\u00e7\u00e3o 3, a modelagem das entidades e dos "
      "relacionamentos; a se\u00e7\u00e3o 4, o passo a passo de execu\u00e7\u00e3o "
      "com Docker Compose; a se\u00e7\u00e3o 5, a documenta\u00e7\u00e3o da API "
      "com Swagger/OpenAPI; a se\u00e7\u00e3o 6, a cole\u00e7\u00e3o Postman; a "
      "se\u00e7\u00e3o 7, a estrutura do banco de dados; a se\u00e7\u00e3o 8, o "
      "tratamento de erros no padr\u00e3o RFC 7807; a se\u00e7\u00e3o 9, a "
      "refer\u00eancia dos endpoints com exemplos de uso; a se\u00e7\u00e3o 10, os "
      "testes automatizados; e a se\u00e7\u00e3o 11, as considera\u00e7\u00f5es "
      "finais.")

    # ===================== 2 ARQUITETURA =====================
    h1("2 ARQUITETURA DA APLICA\u00c7\u00c3O")
    p("A aplica\u00e7\u00e3o segue uma organiza\u00e7\u00e3o em camadas: "
      "controllers REST (pacote controller.v1), servi\u00e7os de neg\u00f3cio "
      "(service), reposit\u00f3rios Spring Data (domain.repository) e entidades "
      "de dom\u00ednio (domain.model), com objetos de transfer\u00eancia de dados "
      "(dto) na entrada e na sa\u00edda e um conjunto dedicado de exce\u00e7\u00f5es "
      "de neg\u00f3cio e respectivos tratadores (exception). A persist\u00eancia "
      "usa Spring Data JPA sobre PostgreSQL 16, e o esquema \u00e9 versionado "
      "pelo Flyway (VMWARE, 2024).")

    quadro("Ficha t\u00e9cnica da solu\u00e7\u00e3o",
           ["Item", "Descri\u00e7\u00e3o"],
           [["Projeto", "restaurant-management (vers\u00e3o 1.0.0)"],
            ["Reposit\u00f3rio", REPO_URL],
            ["Linguagem / framework", "Java 21 (LTS) e Spring Boot 3.3.5"],
            ["Persist\u00eancia", "Spring Data JPA; PostgreSQL 16; Flyway 10"],
            ["Documenta\u00e7\u00e3o", "SpringDoc OpenAPI 2.6.0 (Swagger UI)"],
            ["Seguran\u00e7a de senha", "hash BCrypt (custo 12) e pol\u00edtica de senha forte (@StrongPassword)"],
            ["Empacotamento", "Docker multi-stage e Docker Compose v2"],
            ["Testes", "JUnit 5 + Mockito (70 unit\u00e1rios); Testcontainers (6 de integra\u00e7\u00e3o); "
                       "cobertura por JaCoCo"]],
           [2300, 6770])

    h2("2.1 Decis\u00f5es de projeto")
    bullet("os campos comuns ficam na tabela users e os espec\u00edficos em "
           "customers e restaurant_owners, preservando as restri\u00e7\u00f5es de "
           "integridade de cada coluna \u2014 o que a estrat\u00e9gia SINGLE_TABLE "
           "n\u00e3o permitiria sem tornar as colunas anul\u00e1veis. O custo \u00e9 "
           "uma jun\u00e7\u00e3o por consulta ao subtipo, aceit\u00e1vel no volume "
           "deste dom\u00ednio.", "Heran\u00e7a JOINED para os perfis. ")
    bullet("a vers\u00e3o integra o caminho (/api/v1/...) e os controllers ficam "
           "no pacote controller.v1. \u00c9 expl\u00edcito no endere\u00e7o, "
           "test\u00e1vel no navegador e no Postman, e permite que uma futura v2 "
           "conviva com a v1.", "Versionamento pela URI. ")
    bullet("todas as respostas de erro usam application/problem+json, com o campo "
           "type est\u00e1vel e leg\u00edvel por m\u00e1quina; erros de "
           "valida\u00e7\u00e3o incluem o mapa fieldErrors, apontando cada campo "
           "recusado (NOTTINGHAM; WILDE, 2016).", "Erros no padr\u00e3o RFC 7807. ")
    bullet("o GlobalExceptionHandler responde antes do tratador nativo do Spring "
           "MVC; o tratamento gen\u00e9rico de Exception fica isolado em "
           "UnhandledExceptionHandler, com a menor preced\u00eancia, para n\u00e3o "
           "capturar antes as exce\u00e7\u00f5es que o pr\u00f3prio Spring resolve "
           "corretamente (por exemplo, 415 e 406).",
           "Preced\u00eancia expl\u00edcita entre os tratadores. ")
    bullet("o PUT n\u00e3o recebe senha; a troca exige a senha atual e a "
           "confirma\u00e7\u00e3o da nova, em PATCH .../password. S\u00e3o "
           "opera\u00e7\u00f5es com pr\u00e9-condi\u00e7\u00f5es e riscos "
           "distintos.", "Troca de senha separada da atualiza\u00e7\u00e3o. ")
    bullet("o BaseUserService orquestra as opera\u00e7\u00f5es comuns a qualquer usu\u00e1rio \u2014 "
           "troca de senha e exclus\u00e3o \u2014, a checagem de unicidade de e-mail e "
           "login fica no colaborador UserUniquenessValidator, e os servi\u00e7os "
           "concretos (\u2026ServiceImpl), acessados por interface, tratam apenas o "
           "espec\u00edfico do subtipo. Ver a ader\u00eancia aos princ\u00edpios SOLID na "
           "se\u00e7\u00e3o 2.3.", "Regras comuns em classe base. ")
    bullet("utiliza-se apenas spring-security-crypto para o hash BCrypt com sal, "
           "sem introduzir a cadeia de filtros do Spring Security, "
           "desnecess\u00e1ria nesta fase (o pr\u00f3prio enunciado dispensa o "
           "Spring Security).", "Senhas com BCrypt, sem a stack de filtros. ")

    h2("2.2 Tecnologias utilizadas")
    quadro("Tecnologias e depend\u00eancias utilizadas",
           ["Componente", "Vers\u00e3o", "Papel"],
           [["Java", "21 (LTS)", "Linguagem"],
            ["Spring Boot", "3.3.5", "Framework da aplica\u00e7\u00e3o"],
            ["Spring Data JPA", "3.3.x", "Persist\u00eancia e reposit\u00f3rios"],
            ["PostgreSQL", "16", "Banco de dados relacional"],
            ["Flyway", "10.x", "Versionamento do esquema"],
            ["SpringDoc OpenAPI", "2.6.0", "Especifica\u00e7\u00e3o OpenAPI e Swagger UI"],
            ["spring-security-crypto", "6.x", "Hash BCrypt das senhas"],
            ["Lombok", "1.18.x", "Redu\u00e7\u00e3o de c\u00f3digo repetitivo"],
            ["JUnit 5 e Mockito", "5.x", "Testes unit\u00e1rios"],
            ["Testcontainers", "1.20.x", "Testes de integra\u00e7\u00e3o com PostgreSQL real"],
            ["JaCoCo", "0.8.x", "Cobertura de c\u00f3digo"],
            ["Docker Compose", "v2", "Orquestra\u00e7\u00e3o de aplica\u00e7\u00e3o e banco"]],
           [2600, 1400, 5070])

    h2("2.3 Aplica\u00e7\u00e3o dos princ\u00edpios SOLID")
    p("A organiza\u00e7\u00e3o em camadas descrita acima \u00e9 sustentada pela aplica\u00e7\u00e3o "
      "expl\u00edcita dos cinco princ\u00edpios SOLID, cada um associado a uma decis\u00e3o "
      "concreta do c\u00f3digo: o Princ\u00edpio da Responsabilidade \u00danica (S) levou a "
      "extrair de BaseUserService a checagem de unicidade para o colaborador "
      "dedicado UserUniquenessValidator, de modo que a classe base apenas "
      "orquestre troca de senha e exclus\u00e3o; o Princ\u00edpio Aberto/Fechado (O) "
      "manifesta-se na hierarquia de usu\u00e1rios \u2014 incluir um terceiro perfil (por "
      "exemplo, Entregador) exige apenas uma nova subclasse de "
      "BaseUserService com seu reposit\u00f3rio e controller, sem alterar o c\u00f3digo "
      "existente, tendo como \u00fanico ponto de modifica\u00e7\u00e3o o enum UserType e uma "
      "nova migra\u00e7\u00e3o Flyway; o Princ\u00edpio da Substitui\u00e7\u00e3o de Liskov (L) \u00e9 "
      "respeitado porque Customer e RestaurantOwner substituem User em todos os "
      "servi\u00e7os sem nenhum instanceof ou downcast \u2014 o comportamento espec\u00edfico "
      "do subtipo \u00e9 resolvido por polimorfismo em getUserType(); o Princ\u00edpio da "
      "Segrega\u00e7\u00e3o de Interfaces (I) orientou contratos pequenos e coesos "
      "(UserService exp\u00f5e apenas as duas consultas unificadas; UserBaseRepository "
      "declara s\u00f3 a busca paginada por nome; AuthService tem um \u00fanico m\u00e9todo); e "
      "o Princ\u00edpio da Invers\u00e3o de Depend\u00eancia (D) foi atendido promovendo os "
      "servi\u00e7os a interfaces (CustomerService, RestaurantOwnerService, "
      "UserService, AuthService), das quais os controllers e os testes dependem, "
      "ficando as implementa\u00e7\u00f5es (\u2026ServiceImpl) como detalhe substitu\u00edvel.")
    quadro("Princ\u00edpios SOLID e decis\u00f5es de projeto correspondentes",
           ["Princ\u00edpio", "Decis\u00e3o no c\u00f3digo"],
           [["S \u2014 Responsabilidade \u00danica",
             "UserUniquenessValidator isola a regra de unicidade de e-mail/login; "
             "BaseUserService fica s\u00f3 com troca de senha e exclus\u00e3o; a montagem de "
             "DTO fica em m\u00e9todos toResponse dedicados e AddressMapper converte "
             "entidade\u2194DTO de endere\u00e7o."],
            ["O \u2014 Aberto/Fechado",
             "Novo perfil de usu\u00e1rio = nova subclasse de BaseUserService + "
             "reposit\u00f3rio + controller; nada nas classes existentes muda. Pontos de "
             "modifica\u00e7\u00e3o restantes: o enum UserType e uma migra\u00e7\u00e3o Flyway."],
            ["L \u2014 Substitui\u00e7\u00e3o de Liskov",
             "Customer e RestaurantOwner substituem User sem instanceof/cast nos "
             "servi\u00e7os; UserService.toResponse e AuthService usam getUserType() "
             "polim\u00f3rfico."],
            ["I \u2014 Segrega\u00e7\u00e3o de Interfaces",
             "Contratos enxutos: UserService (2 m\u00e9todos), AuthService (1), "
             "UserBaseRepository (s\u00f3 a busca paginada por nome); reposit\u00f3rios de "
             "subtipo acrescentam apenas findByCpf / findByCnpj."],
            ["D \u2014 Invers\u00e3o de Depend\u00eancia",
             "Controllers e testes dependem das interfaces de servi\u00e7o; o Spring "
             "injeta as implementa\u00e7\u00f5es \u2026ServiceImpl. Reposit\u00f3rios s\u00e3o abstra\u00e7\u00f5es "
             "Spring Data."]],
           [2600, 6470])

    h2("2.4 Reposit\u00f3rio de c\u00f3digo e rastreabilidade")
    p("O projeto \u00e9 mantido em reposit\u00f3rio aberto em " + REPO_URL + ", conforme "
      "exige o item 8 dos entreg\u00e1veis. O reposit\u00f3rio cont\u00e9m o c\u00f3digo-fonte, o "
      "README, a especifica\u00e7\u00e3o Swagger/OpenAPI (gerada em tempo de execu\u00e7\u00e3o e "
      "descrita na se\u00e7\u00e3o 5), a cole\u00e7\u00e3o Postman em JSON e o script que gera este "
      "relat\u00f3rio (docs/gerar_relatorio.py), al\u00e9m dos arquivos-fonte do diagrama "
      "de dom\u00ednio (docs/domain-model.puml e docs/domain-model.mmd). O relat\u00f3rio "
      "em PDF \u00e9 submetido separadamente como entreg\u00e1vel oficial.")

    # ===================== 3 MODELAGEM =====================
    h1("3 MODELAGEM DE ENTIDADES E RELACIONAMENTOS")
    p("O dom\u00ednio possui uma entidade abstrata, User, com dois subtipos "
      "concretos, Customer e RestaurantOwner, e um objeto de valor embutido, "
      "Address. A heran\u00e7a adota a estrat\u00e9gia JOINED: os atributos comuns "
      "ficam na entidade base e os espec\u00edficos em cada subtipo.")
    quadro("Entidades do dom\u00ednio e atributos",
           ["Entidade", "Natureza", "Atributos pr\u00f3prios"],
           [["User", "entidade abstrata (heran\u00e7a JOINED)",
             "id; name; email (\u00fanico); login (\u00fanico); password; "
             "lastModifiedAt; address (embutido)"],
            ["Customer", "subtipo de User",
             "cpf (\u00fanico, opcional); dateOfBirth (opcional)"],
            ["RestaurantOwner", "subtipo de User",
             "restaurantName; cnpj (\u00fanico, opcional)"],
            ["Address", "objeto de valor (embutido em User)",
             "street; number; complement; city; state; zipCode"]],
           [2100, 2900, 4070])
    p("Relacionamentos e cardinalidades:", ind_first=0)
    bullet("User 1:1 Address \u2014 composi\u00e7\u00e3o; o endere\u00e7o n\u00e3o "
           "existe fora do usu\u00e1rio e \u00e9 gravado nas pr\u00f3prias colunas "
           "da tabela users (n\u00e3o h\u00e1 tabela de endere\u00e7o).")
    bullet("User 1:1 Customer e User 1:1 RestaurantOwner \u2014 "
           "especializa\u00e7\u00e3o (rela\u00e7\u00e3o \u00e9-um); cada linha em "
           "customers ou restaurant_owners compartilha a chave prim\u00e1ria com "
           "a linha correspondente em users (PK = FK), com exclus\u00e3o em "
           "cascata.")
    bullet("um usu\u00e1rio pertence a exatamente um subtipo; a coluna user_type "
           "\u00e9 o discriminador da heran\u00e7a.")
    figura_arquivo(
        "Modelo de dom\u00ednio \u2014 diagrama de classes",
        intro="A figura a seguir apresenta o modelo de dom\u00ednio como diagrama de "
              "classes: a entidade abstrata User, o objeto de valor Address "
              "embutido em suas colunas e os dois subtipos concretos, Customer e "
              "RestaurantOwner, cada um em sua tabela e ligado a User pela chave "
              "prim\u00e1ria compartilhada (heran\u00e7a JOINED). O diagrama foi gerado por "
              "ferramenta apropriada (PlantUML), e n\u00e3o desenhado como texto, para "
              "n\u00e3o depender do alinhamento de caracteres ao ser aberto em "
              "diferentes editores.",
        fonte="Fonte: elaborado pela autora com PlantUML (%s)." % ANO,
        obs="Os fontes do diagrama est\u00e3o versionados no reposit\u00f3rio em "
            "docs/domain-model.puml (PlantUML) e docs/domain-model.mmd (Mermaid). "
            "Para regerar a imagem: `plantuml docs/domain-model.puml` (produz "
            "docs/domain-model.png), ou colar o arquivo .mmd em mermaid.live. O "
            "c\u00f3digo-fonte PlantUML \u00e9 reproduzido a seguir, para confer\u00eancia:")
    code_block(
        "@startuml domain-model\n"
        "abstract class User {\n"
        "  +id : Long  <<PK>>\n"
        "  +name : String\n"
        "  +email : String  <<unique>>\n"
        "  +login : String  <<unique>>\n"
        "  #password : String  (hash BCrypt)\n"
        "  +lastModifiedAt : LocalDateTime\n"
        "  +userType : UserType  <<discriminador>>\n"
        "}\n"
        "class Customer {\n"
        "  +cpf : String  <<unique, opcional>>\n"
        "  +dateOfBirth : LocalDate  <<opcional>>\n"
        "}\n"
        "class RestaurantOwner {\n"
        "  +restaurantName : String\n"
        "  +cnpj : String  <<unique, opcional>>\n"
        "}\n"
        "class Address <<@Embeddable>> {\n"
        "  +street +number +complement +city +state +zipCode : String\n"
        "}\n"
        "enum UserType { RESTAURANT_OWNER ; CUSTOMER }\n"
        "User <|-- Customer        : JOINED (PK = FK, 1:1)\n"
        "User <|-- RestaurantOwner : JOINED (PK = FK, 1:1)\n"
        "User *-- \"1\" Address       : composicao (colunas de users)\n"
        "User ..> UserType         : discriminador\n"
        "@enduml")

    h2("3.1 Regras de valida\u00e7\u00e3o e decis\u00f5es de modelagem")
    p("Pol\u00edtica de senha. A senha exige, no m\u00ednimo, oito caracteres, com pelo "
      "menos uma letra min\u00fascula, uma mai\u00fascula, um d\u00edgito e um caractere "
      "especial, sem espa\u00e7os. A regra fica centralizada na anota\u00e7\u00e3o de valida\u00e7\u00e3o "
      "@StrongPassword (Bean Validation), reaproveitada pelos DTOs de cadastro de "
      "cliente, cadastro de dono e troca de senha, evitando repeti\u00e7\u00e3o de "
      "restri\u00e7\u00f5es. O armazenamento continua em hash BCrypt com fator de custo 12. "
      "O enunciado especifica apenas \u201cSenha (String)\u201d, sem regra de complexidade; "
      "a pol\u00edtica mais forte \u00e9 uma decis\u00e3o consciente de boa pr\u00e1tica de seguran\u00e7a, "
      "de baixo risco para o escopo, e as respostas de erro 400 apontam o campo "
      "recusado com a mensagem da regra.")
    p("CPF e CNPJ opcionais. Os campos obrigat\u00f3rios de qualquer usu\u00e1rio, segundo "
      "o enunciado, s\u00e3o nome, e-mail (\u00fanico), login, senha, data da \u00faltima "
      "altera\u00e7\u00e3o e endere\u00e7o; CPF e CNPJ n\u00e3o constam nessa lista \u2014 s\u00e3o atributos "
      "de enriquecimento dos subtipos adicionados pela autora. Por isso "
      "permanecem opcionais: os dois perfis obrigat\u00f3rios funcionam sem documento "
      "fiscal, e a coluna aceita m\u00faltiplos registros sem valor. Quando "
      "informados, por\u00e9m, seguem o formato esperado e t\u00eam unicidade garantida no "
      "banco (uk_customers_cpf, uk_restaurant_owners_cnpj) e verifica\u00e7\u00e3o "
      "antecipada no servi\u00e7o, que devolve 409 apontando o campo em vez de deixar "
      "a viola\u00e7\u00e3o cair no tratamento gen\u00e9rico.")
    p("E-mail e login como identificadores independentes. O enunciado exige "
      "unicidade apenas do e-mail; o login foi mantido tamb\u00e9m \u00fanico, de forma "
      "independente, porque \u00e9 a credencial consumida pelo servi\u00e7o de valida\u00e7\u00e3o "
      "de login (busca por login, ignorando a caixa) e precisa identificar um "
      "\u00fanico usu\u00e1rio sem ambiguidade. S\u00e3o eixos distintos: o e-mail \u00e9 o canal de "
      "contato e eventual recupera\u00e7\u00e3o, e pode mudar sem afetar o acesso; o login "
      "\u00e9 o identificador est\u00e1vel de autentica\u00e7\u00e3o. Ambos s\u00e3o normalizados \u2014 o "
      "e-mail para min\u00fasculas, o login comparado sem diferenciar a caixa \u2014 e a "
      "unicidade vale para o conjunto de todos os usu\u00e1rios, n\u00e3o por subtipo.")

    # ===================== 4 EXECUCAO DOCKER =====================
    h1("4 EXECU\u00c7\u00c3O COM DOCKER COMPOSE")
    p("Pr\u00e9-requisitos: Docker e Docker Compose v2. Os comandos a seguir "
      "s\u00e3o executados na raiz do projeto (diret\u00f3rio "
      "restaurant-management/).")
    code_block(
        "cp .env.example .env            # opcional: ajustar credenciais / porta\n"
        "docker compose up -d --build    # sobe o PostgreSQL 16 e a aplicacao\n"
        "docker compose ps               # confere os containers e o healthcheck\n"
        "docker compose logs -f app      # acompanha o start e o Flyway")
    p("O Compose sobe dois servi\u00e7os: restaurant_db (imagem "
      "postgres:16-alpine, porta 5432) e restaurant_app (imagem constru\u00edda "
      "pelo Dockerfile multi-stage, porta 8080). O container da aplica\u00e7\u00e3o "
      "s\u00f3 inicia depois que o healthcheck do banco passa; na primeira "
      "subida, o Flyway executa o script V1__create_tables.sql e cria o "
      "esquema.")
    quadro("Vari\u00e1veis de ambiente",
           ["Vari\u00e1vel", "Valor padr\u00e3o", "Uso"],
           [["POSTGRES_DB", "restaurant_db", "Nome do banco"],
            ["POSTGRES_USER", "restaurant_user", "Usu\u00e1rio do banco"],
            ["POSTGRES_PASSWORD", "restaurant_pass", "Senha do banco"],
            ["SERVER_PORT", "8080", "Porta exposta pela API"]],
           [2600, 2200, 4270],
           fonte="Fonte: elaborado pela autora a partir de docker-compose.yml e "
                 ".env.example (%s)." % ANO)
    p("Todas as vari\u00e1veis t\u00eam valor padr\u00e3o no docker-compose.yml e "
      "no arquivo .env.example; criar o .env s\u00f3 \u00e9 necess\u00e1rio para "
      "customiz\u00e1-las. Exemplo de conte\u00fado do .env:", ind_first=0)
    code_block("POSTGRES_DB=restaurant_db\n"
               "POSTGRES_USER=restaurant_user\n"
               "POSTGRES_PASSWORD=restaurant_pass\n"
               "SERVER_PORT=8080")
    p("Alternativa sem Docker para a aplica\u00e7\u00e3o: subir apenas o banco "
      "com docker compose up -d db e executar ./mvnw spring-boot:run (Linux e "
      "macOS) ou .\\mvnw.cmd spring-boot:run (Windows), com um PostgreSQL "
      "acess\u00edvel em localhost:5432.")
    figura("Constru\u00e7\u00e3o e subida do ambiente com Docker Compose",
           intro="A figura a seguir registra a execu\u00e7\u00e3o do comando de constru\u00e7\u00e3o "
                 "e subida do ambiente. O Docker Compose primeiro constr\u00f3i a "
                 "imagem da aplica\u00e7\u00e3o a partir do Dockerfile e, em seguida, "
                 "inicia os dois servi\u00e7os; a linha final confirma que o container "
                 "da aplica\u00e7\u00e3o foi iniciado com sucesso.")
    figura("Estado dos containers ap\u00f3s a subida do ambiente",
           intro="Conclu\u00edda a subida, o comando de listagem dos containers "
                 "confirma o estado de cada servi\u00e7o. Na figura a seguir, os dois "
                 "containers aparecem em execu\u00e7\u00e3o; o r\u00f3tulo (healthy) ao lado do "
                 "banco de dados indica que o healthcheck definido no "
                 "docker-compose.yml foi satisfeito \u2014 condi\u00e7\u00e3o necess\u00e1ria para "
                 "que a aplica\u00e7\u00e3o inicialize.")
    figura("Log de inicializa\u00e7\u00e3o da aplica\u00e7\u00e3o e da migra\u00e7\u00e3o do banco",
           intro="Os registros de log da aplica\u00e7\u00e3o, na figura a seguir, "
                 "evidenciam a inicializa\u00e7\u00e3o bem-sucedida do Spring Boot e a "
                 "execu\u00e7\u00e3o autom\u00e1tica do Flyway, que aplica a migra\u00e7\u00e3o "
                 "V1__create_tables e cria o esquema na primeira subida do "
                 "ambiente.")

    # ===================== 5 SWAGGER =====================
    h1("5 DOCUMENTA\u00c7\u00c3O DA API COM SWAGGER/OPENAPI")
    p("Com a aplica\u00e7\u00e3o em execu\u00e7\u00e3o, a documenta\u00e7\u00e3o "
      "interativa \u00e9 gerada pelo SpringDoc OpenAPI.")
    quadro("Recursos de documenta\u00e7\u00e3o da API",
           ["Recurso", "URL"],
           [["Swagger UI", "http://localhost:8080/swagger-ui.html"],
            ["OpenAPI (JSON)", "http://localhost:8080/api-docs"]],
           [2400, 6670])
    p("A API est\u00e1 organizada em quatro agrupamentos (tags): "
      "Autentica\u00e7\u00e3o (valida\u00e7\u00e3o de login e senha), Clientes, "
      "Donos de Restaurante e Usu\u00e1rios (busca unificada por nome e por "
      "identificador). Para cada endpoint, o Swagger apresenta os par\u00e2metros, "
      "o valor de exemplo do corpo da requisi\u00e7\u00e3o e a "
      "execu\u00e7\u00e3o real por meio do recurso Try it out, inclusive nos "
      "cen\u00e1rios de erro, que respondem em application/problem+json.")
    figura("P\u00e1gina inicial da documenta\u00e7\u00e3o na Swagger UI",
           intro="A figura a seguir apresenta a p\u00e1gina inicial da Swagger UI. No "
                 "cabe\u00e7alho constam o t\u00edtulo e a descri\u00e7\u00e3o da API, a vers\u00e3o "
                 "(/api/v1), os tipos de usu\u00e1rio suportados e o padr\u00e3o de erros "
                 "adotado. Abaixo, as opera\u00e7\u00f5es aparecem agrupadas por tag; na "
                 "figura, os grupos Autentica\u00e7\u00e3o e Clientes est\u00e3o expandidos, "
                 "revelando os endpoints de cada recurso.")
    figura("Grupos de opera\u00e7\u00f5es Donos de Restaurante e Usu\u00e1rios na Swagger UI",
           intro="A figura a seguir d\u00e1 sequ\u00eancia \u00e0 anterior e exibe os grupos "
                 "Donos de Restaurante e Usu\u00e1rios expandidos. O grupo Usu\u00e1rios "
                 "concentra as consultas consolidadas, que permitem localizar "
                 "qualquer usu\u00e1rio sem que o consumidor precise saber previamente "
                 "se ele \u00e9 cliente ou dono de restaurante.")
    figura("Vis\u00e3o geral das opera\u00e7\u00f5es da API na Swagger UI",
           intro="Com todas as tags recolhidas, obt\u00e9m-se a vis\u00e3o geral da API, "
                 "como na figura a seguir: um panorama enxuto de todos os "
                 "recursos e das opera\u00e7\u00f5es dispon\u00edveis, \u00fatil para conferir a "
                 "cobertura dos requisitos.")
    figura("Documenta\u00e7\u00e3o da opera\u00e7\u00e3o de listagem de clientes",
           intro="A figura a seguir detalha a opera\u00e7\u00e3o de listagem de clientes "
                 "(GET /api/v1/customers). S\u00e3o exibidos o par\u00e2metro de consulta "
                 "name, usado no filtro por nome, e a resposta 200, com o exemplo "
                 "do corpo retornado.")
    figura("Requisi\u00e7\u00e3o de cadastro montada pelo recurso Try it out",
           intro="Ao acionar o recurso Try it out em uma opera\u00e7\u00e3o de escrita, a "
                 "Swagger UI monta a requisi\u00e7\u00e3o e disponibiliza o comando curl "
                 "equivalente. A figura a seguir mostra a se\u00e7\u00e3o Parameters, o "
                 "corpo da requisi\u00e7\u00e3o preenchido e o comando gerado.")
    figura("Resposta de sucesso da opera\u00e7\u00e3o de cadastro",
           intro="A figura a seguir apresenta a resposta de sucesso da opera\u00e7\u00e3o "
                 "executada: o c\u00f3digo HTTP retornado, o corpo devolvido pelo "
                 "servidor e o esquema documentado da resposta.")
    figura("Respostas de erro documentadas para a opera\u00e7\u00e3o",
           intro="Al\u00e9m da resposta de sucesso, cada opera\u00e7\u00e3o documenta as "
                 "respostas de erro previstas. A figura a seguir lista os c\u00f3digos "
                 "400, 404 e 409, cada um com o respectivo exemplo de corpo no "
                 "formato application/problem+json.")
    figura("Corpo de resposta de erro no padr\u00e3o RFC 7807",
           intro="A figura a seguir exibe o corpo completo de uma resposta 409, "
                 "com todos os campos do padr\u00e3o RFC 7807 \u2014 type, title, status, "
                 "detail, instance e timestamp \u2014, demonstrando a padroniza\u00e7\u00e3o das "
                 "mensagens de erro descrita na se\u00e7\u00e3o 8.")

    # ===================== 6 POSTMAN =====================
    h1("6 COLE\u00c7\u00c3O POSTMAN")
    p("A cole\u00e7\u00e3o est\u00e1 no arquivo "
      "postman/restaurant-management.postman_collection.json. No Postman, use "
      "Import e arraste o arquivo; a vari\u00e1vel de cole\u00e7\u00e3o base_url "
      "j\u00e1 vem preenchida com http://localhost:8080/api/v1.")
    p("As pastas s\u00e3o numeradas na ordem de execu\u00e7\u00e3o, porque cada "
      "uma depende do que a anterior cadastrou. Os identificadores s\u00e3o "
      "capturados dos pr\u00f3prios cadastros e cada execu\u00e7\u00e3o gera "
      "e-mails, logins, CPF e CNPJ distintos \u2014 a cole\u00e7\u00e3o pode "
      "rodar v\u00e1rias vezes no mesmo banco sem violar as restri\u00e7\u00f5es "
      "de unicidade.")
    quadro("Pastas da cole\u00e7\u00e3o Postman e cen\u00e1rios cobertos",
           ["Pasta", "Cen\u00e1rios"],
           [["1. Cadastro de usu\u00e1rios",
             "cadastro de dono e de cliente (201); recusa por e-mail j\u00e1 "
             "usado (409); recusa por campos obrigat\u00f3rios faltando (400)."],
            ["2. Valida\u00e7\u00e3o de login",
             "login de dono e de cliente (200); recusa por senha incorreta "
             "(401), login inexistente (401) e campos vazios (400)."],
            ["3. Busca de usu\u00e1rios",
             "busca por nome (donos, clientes e unificada); busca sem filtro; "
             "busca por identificador; identificador inexistente (404)."],
            ["4. Atualiza\u00e7\u00e3o de dados",
             "atualiza\u00e7\u00e3o de dono e de cliente (200), inclusive sem "
             "enviar endere\u00e7o; recusas 404, 409 e 400."],
            ["5. Troca de senha",
             "recusas 422 (senha atual incorreta / confirma\u00e7\u00e3o "
             "divergente) e 400 (nova senha curta); troca efetiva (204); login "
             "com a senha antiga falha (401) e com a nova funciona (200)."],
            ["6. Exclus\u00e3o",
             "exclus\u00e3o de cliente e de dono (204); usu\u00e1rio exclu\u00eddo "
             "n\u00e3o \u00e9 mais encontrado (404); exclus\u00e3o de usu\u00e1rio "
             "inexistente (404)."]],
           [2500, 6570])
    p("A execu\u00e7\u00e3o completa \u00e9 feita pelo Collection Runner, com as "
      "pastas na ordem de 1 a 6.")
    figura("Cole\u00e7\u00e3o importada no Postman",
           intro="A figura a seguir apresenta a cole\u00e7\u00e3o j\u00e1 importada no Postman, "
                 "com as seis pastas organizadas na barra lateral. A numera\u00e7\u00e3o "
                 "das pastas reflete a ordem de execu\u00e7\u00e3o recomendada, pois cada "
                 "cen\u00e1rio reutiliza dados criados pelo anterior.")
    figura("Cadastro de dono de restaurante bem-sucedido no Postman",
           intro="A figura a seguir mostra a execu\u00e7\u00e3o isolada da requisi\u00e7\u00e3o de "
                 "cadastro de dono de restaurante. O corpo enviado e a resposta "
                 "201 Created, com o recurso rec\u00e9m-criado, confirmam o "
                 "funcionamento do endpoint de cadastro.")
    figura("Recusa de cadastro por e-mail j\u00e1 utilizado no Postman",
           intro="A figura a seguir registra o cen\u00e1rio de erro correspondente: "
                 "ao tentar cadastrar um usu\u00e1rio com e-mail j\u00e1 utilizado, a API "
                 "responde 409 Conflict com o corpo no padr\u00e3o ProblemDetail, "
                 "detalhando o conflito.")
    figura("Execu\u00e7\u00e3o encadeada dos cen\u00e1rios de cadastro no Postman",
           intro="A figura a seguir mostra o envio bem-sucedido do cadastro de "
                 "dono de restaurante com as pastas iniciais da cole\u00e7\u00e3o abertas "
                 "na barra lateral, evidenciando o encadeamento dos cen\u00e1rios de "
                 "cadastro, valida\u00e7\u00e3o de login e busca.")
    figura("Recusa de cadastro por campos obrigat\u00f3rios ausentes no Postman",
           intro="A figura a seguir apresenta o cen\u00e1rio de recusa por campos "
                 "obrigat\u00f3rios ausentes: a requisi\u00e7\u00e3o de cadastro de cliente sem "
                 "os campos exigidos retorna 400, com o mapa fieldErrors "
                 "apontando cada campo recusado.")
    figura("Recusa de troca de senha por senha atual incorreta no Postman",
           intro="A figura a seguir exercita o endpoint exclusivo de troca de "
                 "senha informando a senha atual incorreta. A resposta 422 "
                 "(Unprocessable Entity) confirma, na pr\u00e1tica, a distin\u00e7\u00e3o entre "
                 "falha de autentica\u00e7\u00e3o (401) e falha de pr\u00e9-condi\u00e7\u00e3o de neg\u00f3cio "
                 "(422).")
    figura("Execu\u00e7\u00e3o completa da cole\u00e7\u00e3o no Collection Runner",
           intro="A figura a seguir apresenta o resultado da execu\u00e7\u00e3o completa "
                 "da cole\u00e7\u00e3o pelo Collection Runner. A mensagem de conclus\u00e3o e a "
                 "marca\u00e7\u00e3o de todas as verifica\u00e7\u00f5es como aprovadas comprovam a "
                 "cobertura dos cen\u00e1rios exigidos pelo enunciado.")

    # ===================== 7 BANCO =====================
    h1("7 ESTRUTURA DO BANCO DE DADOS")
    p("O esquema \u00e9 criado pelo Flyway a partir do script "
      "src/main/resources/db/migration/V1__create_tables.sql.")
    quadro("Tabelas do banco de dados",
           ["Tabela", "Colunas"],
           [["users",
             "id; user_type (discriminador); name; email (\u00fanico); login "
             "(\u00fanico); password; last_modified_at; e o endere\u00e7o "
             "embutido: street, number, complement, city, state, zip_code."],
            ["customers",
             "id (FK para users); cpf (\u00fanico); date_of_birth."],
            ["restaurant_owners",
             "id (FK para users); restaurant_name; cnpj (\u00fanico)."]],
           [2200, 6870])
    p("As tabelas filhas referenciam users por chave estrangeira com "
      "exclus\u00e3o em cascata. O \u00edndice idx_users_name \u00e9 criado sobre "
      "LOWER(name), para a busca por nome sem diferenciar mai\u00fasculas de "
      "min\u00fasculas. A coluna last_modified_at \u00e9 mantida pelo Hibernate "
      "via @UpdateTimestamp.")
    p("A estrutura pode ser inspecionada diretamente pelo container do banco:",
      ind_first=0)
    code_block(
        'docker compose exec db psql -U restaurant_user -d restaurant_db -c "\\dt"\n'
        'docker compose exec db psql -U restaurant_user -d restaurant_db -c "\\d users"')
    figura("Estrutura do banco de dados inspecionada via psql",
           intro="A figura a seguir re\u00fane as sa\u00eddas de dois comandos executados "
                 "diretamente no container do banco: a listagem das rela\u00e7\u00f5es "
                 "(\\dt), que confirma a cria\u00e7\u00e3o das tr\u00eas tabelas do dom\u00ednio "
                 "al\u00e9m da tabela de controle do Flyway; e a descri\u00e7\u00e3o da tabela "
                 "users (\\d users), com as colunas, os tipos, as restri\u00e7\u00f5es de "
                 "unicidade de e-mail e login e o \u00edndice idx_users_name sobre "
                 "LOWER(name).")

    # ===================== 8 ERROS =====================
    h1("8 TRATAMENTO DE ERROS (RFC 7807)")
    p("Todas as respostas de erro usam o tipo de m\u00eddia "
      "application/problem+json, conforme o padr\u00e3o Problem Details "
      "(NOTTINGHAM; WILDE, 2016). Exemplo de corpo:")
    code_block(
        '{\n'
        '  "type": "https://restaurant-api.com/errors/login-conflict",\n'
        '  "title": "Login Ja Cadastrado",\n'
        '  "status": 409,\n'
        '  "detail": "Ja existe um usuario cadastrado com o login: maria.silva",\n'
        '  "instance": "/api/v1/customers",\n'
        '  "timestamp": "2026-08-24T21:00:00Z"\n'
        '}')
    quadro("Padr\u00f5es de resposta de erro",
           ["Status", "type", "Situa\u00e7\u00e3o"],
           [["400", "validation-error", "Bean Validation recusou campos; inclui fieldErrors."],
            ["400", "malformed-json", "Corpo ausente ou JSON malformado."],
            ["400", "invalid-parameter", "Par\u00e2metro de caminho ou consulta com tipo incompat\u00edvel."],
            ["401", "invalid-credentials", "Login inexistente ou senha incorreta."],
            ["404", "user-not-found", "Nenhum usu\u00e1rio com o identificador informado."],
            ["404", "resource-not-found", "Rota inexistente."],
            ["405", "method-not-allowed", "M\u00e9todo HTTP n\u00e3o suportado pelo endpoint."],
            ["409", "email-conflict", "E-mail j\u00e1 cadastrado."],
            ["409", "login-conflict", "Login j\u00e1 cadastrado."],
            ["409", "cpf-conflict", "CPF j\u00e1 cadastrado."],
            ["409", "cnpj-conflict", "CNPJ j\u00e1 cadastrado."],
            ["409", "data-integrity-conflict", "Viola\u00e7\u00e3o de restri\u00e7\u00e3o do banco n\u00e3o coberta acima."],
            ["422", "invalid-password", "Senha atual incorreta ou confirma\u00e7\u00e3o divergente."],
            ["500", "internal-error", "Falha n\u00e3o prevista."]],
           [900, 2500, 5670],
           fonte="Fonte: elaborado pela autora com base em Nottingham e Wilde "
                 "(2016) (%s)." % ANO)
    p("A distin\u00e7\u00e3o entre 401 e 422 \u00e9 intencional: 401 responde a "
      "quem n\u00e3o comprovou identidade no login; 422 responde a um pedido bem "
      "formado cuja pr\u00e9-condi\u00e7\u00e3o de neg\u00f3cio falhou, como "
      "trocar a senha informando a senha atual errada.")

    # ===================== 9 ENDPOINTS =====================
    h1("9 REFER\u00caNCIA DOS ENDPOINTS")
    h2("9.1 Autentica\u00e7\u00e3o")
    quadro("Endpoints de autentica\u00e7\u00e3o",
           ["M\u00e9todo", "Caminho", "Descri\u00e7\u00e3o"],
           [["POST", "/api/v1/auth/login", "Valida login e senha"]],
           [1100, 3400, 4570])
    h2("9.2 Clientes")
    quadro("Endpoints de clientes",
           ["M\u00e9todo", "Caminho", "Descri\u00e7\u00e3o"],
           [["POST", "/api/v1/customers", "Cadastra"],
            ["GET", "/api/v1/customers", "Lista paginada (page, size, sort), com filtro opcional ?name="],
            ["GET", "/api/v1/customers/{id}", "Busca por identificador"],
            ["PUT", "/api/v1/customers/{id}", "Atualiza dados cadastrais"],
            ["PATCH", "/api/v1/customers/{id}/password", "Troca a senha"],
            ["DELETE", "/api/v1/customers/{id}", "Exclui"]],
           [1100, 3400, 4570])
    h2("9.3 Donos de restaurante")
    quadro("Endpoints de donos de restaurante",
           ["M\u00e9todo", "Caminho", "Descri\u00e7\u00e3o"],
           [["POST", "/api/v1/restaurant-owners", "Cadastra"],
            ["GET", "/api/v1/restaurant-owners", "Lista paginada (page, size, sort), com filtro opcional ?name="],
            ["GET", "/api/v1/restaurant-owners/{id}", "Busca por identificador"],
            ["PUT", "/api/v1/restaurant-owners/{id}", "Atualiza dados cadastrais"],
            ["PATCH", "/api/v1/restaurant-owners/{id}/password", "Troca a senha"],
            ["DELETE", "/api/v1/restaurant-owners/{id}", "Exclui"]],
           [1100, 3900, 4070])
    h2("9.4 Usu\u00e1rios")
    quadro("Endpoints de usu\u00e1rios",
           ["M\u00e9todo", "Caminho", "Descri\u00e7\u00e3o"],
           [["GET", "/api/v1/users", "Lista paginada de clientes e donos juntos, com filtro ?name="],
            ["GET", "/api/v1/users/{id}", "Busca qualquer usu\u00e1rio por identificador"]],
           [1100, 3000, 4970])
    p("Os tr\u00eas endpoints de listagem (GET /customers, /restaurant-owners e "
      "/users) s\u00e3o paginados com o suporte padr\u00e3o do Spring Data: aceitam os "
      "par\u00e2metros de consulta page (base zero; padr\u00e3o 0), size (padr\u00e3o 20, "
      "m\u00e1ximo 100) e sort (ex.: sort=name,asc), combin\u00e1veis com o filtro "
      "opcional name. A resposta \u00e9 um envelope com content (a p\u00e1gina de itens) e "
      "os metadados page, size, totalElements, totalPages e last. A busca por id "
      "n\u00e3o \u00e9 paginada.")

    h2("9.5 Exemplos de uso")
    p("a) Cadastro de cliente \u2014 POST /api/v1/customers", ind_first=0, bold=True)
    code_block(
        'curl -X POST http://localhost:8080/api/v1/customers \\\n'
        '  -H "Content-Type: application/json" \\\n'
        '  -d \'{\n'
        '        "name": "Maria Oliveira",\n'
        '        "email": "maria@email.com",\n'
        '        "login": "maria.oliveira",\n'
        '        "password": "Senha@456",\n'
        '        "cpf": "123.456.789-00",\n'
        '        "dateOfBirth": "1995-07-15",\n'
        '        "address": { "street": "Rua das Flores", "number": "123",\n'
        '                     "city": "Sao Paulo", "state": "SP", "zipCode": "01310-100" }\n'
        '      }\'')
    p("Resposta 201 Created:", ind_first=0)
    code_block(
        '{\n'
        '  "id": 2, "userType": "CUSTOMER",\n'
        '  "name": "Maria Oliveira", "email": "maria@email.com",\n'
        '  "login": "maria.oliveira", "lastModifiedAt": "2026-08-30T21:00:00",\n'
        '  "address": { "street": "Rua das Flores", "number": "123", "complement": null,\n'
        '               "city": "Sao Paulo", "state": "SP", "zipCode": "01310-100" },\n'
        '  "cpf": "123.456.789-00", "dateOfBirth": "1995-07-15"\n'
        '}')
    p("O cadastro com e-mail j\u00e1 existente responde 409 Conflict, com corpo "
      "em application/problem+json (ver se\u00e7\u00e3o 8).")
    p("b) Valida\u00e7\u00e3o de login \u2014 POST /api/v1/auth/login", ind_first=0,
      bold=True)
    code_block(
        'curl -X POST http://localhost:8080/api/v1/auth/login \\\n'
        '  -H "Content-Type: application/json" \\\n'
        '  -d \'{ "login": "maria.oliveira", "password": "Senha@456" }\'')
    p("Resposta 200 OK:", ind_first=0)
    code_block(
        '{ "id": 2, "name": "Maria Oliveira", "login": "maria.oliveira",\n'
        '  "email": "maria@email.com", "userType": "CUSTOMER",\n'
        '  "message": "Login realizado com sucesso" }')
    p("Login inv\u00e1lido responde 401 (type invalid-credentials).")
    p("c) Troca de senha (endpoint exclusivo) \u2014 "
      "PATCH /api/v1/customers/{id}/password", ind_first=0, bold=True)
    code_block(
        'curl -X PATCH http://localhost:8080/api/v1/customers/2/password \\\n'
        '  -H "Content-Type: application/json" \\\n'
        '  -d \'{ "currentPassword": "Senha@456",\n'
        '        "newPassword": "NovaSenha@789",\n'
        '        "confirmNewPassword": "NovaSenha@789" }\'')
    p("O sucesso responde 204 No Content. Senha atual incorreta ou "
      "confirma\u00e7\u00e3o divergente responde 422 (type invalid-password); "
      "nova senha com menos de seis caracteres responde 400.")
    p("d) Atualiza\u00e7\u00e3o cadastral (endpoint distinto do de senha) \u2014 "
      "PUT /api/v1/customers/{id}", ind_first=0, bold=True)
    code_block(
        'curl -X PUT http://localhost:8080/api/v1/customers/2 \\\n'
        '  -H "Content-Type: application/json" \\\n'
        '  -d \'{ "name": "Maria O. Souza", "email": "maria.souza@email.com",\n'
        '        "login": "maria.oliveira", "cpf": "123.456.789-00" }\'')
    p("Responde 200 OK com o cadastro atualizado. O corpo n\u00e3o aceita senha. "
      "Endere\u00e7o ausente no corpo significa manter o atual, n\u00e3o "
      "apag\u00e1-lo.")
    p("e) Busca por nome, paginada \u2014 GET /api/v1/users?name=maria&page=0&size=20",
      ind_first=0, bold=True)
    code_block('curl "http://localhost:8080/api/v1/users?name=maria&page=0&size=20&sort=name,asc"')
    p("Resposta 200 OK (envelope de p\u00e1gina):", ind_first=0)
    code_block(
        '{\n'
        '  "content": [ { "id": 2, "userType": "CUSTOMER", "name": "Maria Oliveira", ... } ],\n'
        '  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1, "last": true\n'
        '}')
    p("Responde com a p\u00e1gina de clientes e donos cujo nome cont\u00e9m o termo, sem "
      "diferenciar mai\u00fasculas de min\u00fasculas. Sem o par\u00e2metro name, retorna todos "
      "os usu\u00e1rios, tamb\u00e9m paginados.")

    # ===================== 10 TESTES =====================
    h1("10 TESTES AUTOMATIZADOS")
    p("Os testes unit\u00e1rios s\u00e3o executados com o Maven Wrapper; a su\u00edte completa, "
      "incluindo os testes de integra\u00e7\u00e3o, roda com o objetivo verify:", ind_first=0)
    code_block("./mvnw test      # 70 testes unitarios (JUnit 5 + Mockito)\n"
               "./mvnw verify    # + 6 testes de integracao com Testcontainers (exige Docker)")
    p("S\u00e3o 70 testes unit\u00e1rios, distribu\u00eddos entre:", ind_first=0)
    bullet("servi\u00e7os, com Mockito: AuthServiceTest, CustomerServiceTest, "
           "RestaurantOwnerServiceTest e UserServiceTest \u2014 cobrem as regras de "
           "neg\u00f3cio, a unicidade delegada a UserUniquenessValidator e a "
           "pagina\u00e7\u00e3o;")
    bullet("camada web, com @WebMvcTest: AuthControllerTest, "
           "CustomerControllerTest, RestaurantOwnerControllerTest e "
           "UserControllerTest \u2014 rotas, c\u00f3digos de status, o envelope de p\u00e1gina e "
           "o corpo de erro RFC 7807;")
    bullet("tratamento de erros: GlobalExceptionHandlerTest e "
           "UnhandledExceptionHandlerTest.")
    p("Al\u00e9m deles, o teste de integra\u00e7\u00e3o SchemaConstraintsIT sobe um PostgreSQL "
      "real com Testcontainers (imagem postgres:16-alpine) e valida, contra o "
      "banco de verdade, o que antes s\u00f3 era conferido manualmente com docker "
      "compose e psql: que a migra\u00e7\u00e3o V1 do Flyway \u00e9 aplicada e cria as tabelas "
      "e o \u00edndice idx_users_name, e que as restri\u00e7\u00f5es de unicidade de e-mail, "
      "login, CPF e CNPJ s\u00e3o efetivamente impostas (rejei\u00e7\u00e3o com viola\u00e7\u00e3o de "
      "integridade), incluindo o caso de CPF nulo, que \u00e9 opcional e n\u00e3o \u00fanico. "
      "Sufixado com IT, roda na fase verify pelo maven-failsafe-plugin; sem "
      "Docker dispon\u00edvel, a classe \u00e9 ignorada, sem quebrar o build.")
    p("A cobertura \u00e9 medida pelo JaCoCo (relat\u00f3rio em target/site/jacoco/ ap\u00f3s "
      "mvn test). Na su\u00edte de testes unit\u00e1rios, a cobertura \u00e9 de "
      "aproximadamente 83% das instru\u00e7\u00f5es e 81% das linhas do c\u00f3digo de "
      "produ\u00e7\u00e3o, concentrando-se nas camadas de servi\u00e7o, web e tratamento de "
      "erros.")
    figura("In\u00edcio da execu\u00e7\u00e3o da su\u00edte de testes automatizados",
           intro="A figura a seguir mostra o in\u00edcio da execu\u00e7\u00e3o da su\u00edte: o "
                 "Maven Wrapper compila o projeto, o Spring Boot \u00e9 inicializado "
                 "no contexto de teste e os casos come\u00e7am a ser executados.")
    figura("Conclus\u00e3o da su\u00edte de testes automatizados",
           intro="A figura a seguir apresenta o encerramento da execu\u00e7\u00e3o, com o "
                 "resultado BUILD SUCCESS e a contagem final: 70 testes "
                 "executados, sem falhas e sem erros.")
    figura("Relat\u00f3rio de cobertura de c\u00f3digo gerado pelo JaCoCo",
           intro="A figura a seguir apresenta o relat\u00f3rio de cobertura do JaCoCo "
                 "(target/site/jacoco/index.html), com o percentual de "
                 "instru\u00e7\u00f5es e de ramos exercitados por pacote.")

    # ===================== 11 CONSIDERACOES FINAIS =====================
    h1("11 CONSIDERA\u00c7\u00d5ES FINAIS")
    p("O backend entregue atende integralmente aos requisitos funcionais da "
      "Fase 1: cadastro, consulta, atualiza\u00e7\u00e3o e exclus\u00e3o dos dois "
      "perfis de usu\u00e1rio; troca de senha em endpoint exclusivo, separado do "
      "endpoint de atualiza\u00e7\u00e3o cadastral; registro da data da "
      "\u00faltima altera\u00e7\u00e3o; busca por nome; unicidade de e-mail; e "
      "servi\u00e7o de valida\u00e7\u00e3o de login.")
    p("Quanto aos crit\u00e9rios de avalia\u00e7\u00e3o, a solu\u00e7\u00e3o "
      "implementa versionamento de API pela URI, o padr\u00e3o ProblemDetail "
      "(RFC 7807) para as respostas de erro, documenta\u00e7\u00e3o "
      "Swagger/OpenAPI com exemplos de sucesso e de erro, banco relacional "
      "PostgreSQL em container e cole\u00e7\u00e3o Postman cobrindo os "
      "cen\u00e1rios pedidos. A qualidade de c\u00f3digo \u00e9 sustentada pela aplica\u00e7\u00e3o "
      "expl\u00edcita dos princ\u00edpios SOLID (se\u00e7\u00e3o 2.3), com servi\u00e7os expostos por "
      "interface, hierarquia de usu\u00e1rios aberta a extens\u00e3o e a regra de "
      "unicidade isolada em um colaborador dedicado. Como desafio extra, foram "
      "inclu\u00eddos 70 testes unit\u00e1rios com JUnit 5 e Mockito e 6 testes de "
      "integra\u00e7\u00e3o com Testcontainers contra um PostgreSQL real, com cobertura "
      "medida por JaCoCo (aprox. 81% das linhas).")
    p("Como evolu\u00e7\u00e3o para as pr\u00f3ximas fases, preveem-se a "
      "autentica\u00e7\u00e3o com Spring Security e JWT, o cadastro de "
      "restaurantes e card\u00e1pios, e os fluxos de avalia\u00e7\u00f5es e de "
      "pedidos on-line mencionados na vis\u00e3o geral do produto.")

    # ===================== REFERENCIAS =====================
    h1_sem_numero("REFER\u00caNCIAS")
    refs = [
        "ASSOCIA\u00c7\u00c3O BRASILEIRA DE NORMAS T\u00c9CNICAS. NBR 14724: "
        "informa\u00e7\u00e3o e documenta\u00e7\u00e3o: trabalhos acad\u00eamicos: "
        "apresenta\u00e7\u00e3o. Rio de Janeiro: ABNT, 2011.",
        "DOCKER INC. Compose file reference. [S. l.], 2024. Dispon\u00edvel em: "
        "https://docs.docker.com/compose/compose-file/. Acesso em: %s." % DATA_ACESSO,
        "FIAP. Tech Challenge \u2013 Fase 1: Arquitetura e Desenvolvimento Java. "
        "S\u00e3o Paulo: FIAP, [2024]. Enunciado do desafio.",
        "NOTTINGHAM, M.; WILDE, E. RFC 7807: Problem Details for HTTP APIs. "
        "[S. l.]: Internet Engineering Task Force, 2016. Dispon\u00edvel em: "
        "https://www.rfc-editor.org/rfc/rfc7807. Acesso em: %s." % DATA_ACESSO,
        "REDGATE SOFTWARE. Flyway documentation. [S. l.], 2024. Dispon\u00edvel "
        "em: https://documentation.red-gate.com/flyway. Acesso em: %s." % DATA_ACESSO,
        "SMARTBEAR SOFTWARE. OpenAPI Specification: version 3.0.3. [S. l.], "
        "2020. Dispon\u00edvel em: https://spec.openapis.org/oas/v3.0.3. Acesso "
        "em: %s." % DATA_ACESSO,
        "THE POSTGRESQL GLOBAL DEVELOPMENT GROUP. PostgreSQL 16 documentation. "
        "[S. l.], 2023. Dispon\u00edvel em: https://www.postgresql.org/docs/16/. "
        "Acesso em: %s." % DATA_ACESSO,
        "VMWARE. Spring Boot reference documentation: version 3.3.5. [S. l.], "
        "2024. Dispon\u00edvel em: "
        "https://docs.spring.io/spring-boot/docs/3.3.5/reference/html/. Acesso "
        "em: %s." % DATA_ACESSO,
        "VMWARE. Spring Data JPA reference documentation. [S. l.], 2024. "
        "Dispon\u00edvel em: https://docs.spring.io/spring-data/jpa/reference/. "
        "Acesso em: %s." % DATA_ACESSO,
    ]
    for r in refs:
        p(r, style="Referencia")


# --------------------------------------------------------------------------- #
# Pacote OOXML                                                                 #
# --------------------------------------------------------------------------- #

CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
<Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
<Override PartName="/word/header2.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>
<Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>"""

RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""

DOC_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header" Target="header2.xml"/>
<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
</Relationships>"""

SETTINGS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:updateFields w:val="true"/>
<w:defaultTabStop w:val="708"/>
</w:settings>"""

NUMBERING = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:abstractNum w:abstractNumId="0">
<w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val="&#8226;"/>
<w:lvlJc w:val="left"/><w:pPr><w:ind w:left="714" w:hanging="357"/></w:pPr>
<w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/></w:rPr></w:lvl>
</w:abstractNum>
<w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
</w:numbering>"""

# Arial 12 (sz 24) padrao; 1,5 linha (line 360); recuo primeira linha 1,25 cm (709 twips)
STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:docDefaults><w:rPrDefault><w:rPr>
<w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="24"/><w:szCs w:val="24"/>
<w:lang w:val="pt-BR"/></w:rPr></w:rPrDefault>
<w:pPrDefault><w:pPr><w:spacing w:after="0" w:line="360" w:lineRule="auto"/>
<w:jc w:val="both"/></w:pPr></w:pPrDefault></w:docDefaults>
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/>
<w:pPr><w:spacing w:after="0" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="709"/>
<w:jc w:val="both"/></w:pPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:pageBreakBefore/>
<w:spacing w:before="0" w:after="360" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="left"/><w:outlineLvl w:val="0"/></w:pPr>
<w:rPr><w:b/><w:caps/><w:color w:val="000000"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/>
<w:spacing w:before="360" w:after="180" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="left"/><w:outlineLvl w:val="1"/></w:pPr>
<w:rPr><w:b/><w:color w:val="000000"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/>
<w:spacing w:before="240" w:after="120" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="left"/><w:outlineLvl w:val="2"/></w:pPr>
<w:rPr><w:color w:val="000000"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="TituloPreTextual"><w:name w:val="Titulo Pre Textual"/>
<w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:pageBreakBefore/>
<w:spacing w:before="0" w:after="360" w:line="360" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="center"/></w:pPr><w:rPr><w:b/><w:caps/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Resumo"><w:name w:val="Resumo ABNT"/><w:basedOn w:val="Normal"/>
<w:pPr><w:spacing w:after="240" w:line="240" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="both"/></w:pPr></w:style>
<w:style w:type="paragraph" w:styleId="Natureza"><w:name w:val="Natureza"/><w:basedOn w:val="Normal"/>
<w:pPr><w:spacing w:after="120" w:line="240" w:lineRule="auto"/><w:ind w:left="4536" w:firstLine="0"/>
<w:jc w:val="both"/></w:pPr></w:style>
<w:style w:type="paragraph" w:styleId="Legenda"><w:name w:val="caption"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:qFormat/><w:pPr>
<w:spacing w:before="120" w:after="0" w:line="240" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="center"/></w:pPr><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Fonte"><w:name w:val="Fonte Ilustracao"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:pPr>
<w:spacing w:before="0" w:after="240" w:line="240" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="center"/></w:pPr><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Referencia"><w:name w:val="Referencia ABNT"/><w:basedOn w:val="Normal"/>
<w:pPr><w:spacing w:after="240" w:line="240" w:lineRule="auto"/><w:ind w:firstLine="0"/>
<w:jc w:val="left"/></w:pPr></w:style>
<w:style w:type="paragraph" w:styleId="ListBullet"><w:name w:val="List Bullet"/><w:basedOn w:val="Normal"/>
<w:pPr><w:spacing w:after="0" w:line="360" w:lineRule="auto"/><w:ind w:left="714" w:hanging="357"/>
<w:contextualSpacing/><w:jc w:val="both"/></w:pPr></w:style>
</w:styles>"""


def core_xml():
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            '<cp:coreProperties '
            'xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" '
            'xmlns:dc="http://purl.org/dc/elements/1.1/" '
            'xmlns:dcterms="http://purl.org/dc/terms/" '
            'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
            '<dc:title>Relatorio Tecnico - Tech Challenge Fase 1</dc:title>'
            '<dc:subject>Sistema de gestao de restaurantes - API REST</dc:subject>'
            '<dc:creator>Mikaela Perez Guedes</dc:creator>'
            '<cp:lastModifiedBy>Mikaela Perez Guedes</cp:lastModifiedBy>'
            '<dcterms:created xsi:type="dcterms:W3CDTF">%s</dcterms:created>'
            '<dcterms:modified xsi:type="dcterms:W3CDTF">%s</dcterms:modified>'
            '</cp:coreProperties>' % (now, now))


APP_XML = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
           '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">'
           '<Application>Microsoft Office Word</Application></Properties>')


def header_xml():
    rpr = _rpr(sz=20)
    campo = ('<w:r>%s<w:fldChar w:fldCharType="begin"/></w:r>'
             '<w:r>%s<w:instrText xml:space="preserve"> = </w:instrText></w:r>'
             '<w:r>%s<w:fldChar w:fldCharType="begin"/></w:r>'
             '<w:r>%s<w:instrText xml:space="preserve">PAGE</w:instrText></w:r>'
             '<w:r>%s<w:fldChar w:fldCharType="end"/></w:r>'
             '<w:r>%s<w:instrText xml:space="preserve"> - 1 </w:instrText></w:r>'
             '<w:r>%s<w:fldChar w:fldCharType="separate"/></w:r>'
             '<w:r>%s<w:t>1</w:t></w:r>'
             '<w:r>%s<w:fldChar w:fldCharType="end"/></w:r>'
             % (rpr, rpr, rpr, rpr, rpr, rpr, rpr, rpr, rpr))
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            '<w:hdr %s><w:p><w:pPr><w:jc w:val="right"/>'
            '<w:spacing w:after="0" w:line="240" w:lineRule="auto"/>'
            '<w:ind w:firstLine="0"/><w:rPr><w:sz w:val="20"/></w:rPr></w:pPr>'
            '%s</w:p></w:hdr>' % (NS, campo))


def document_xml():
    sect_final = ('<w:sectPr>'
                  '<w:headerReference w:type="default" r:id="rId3"/>'
                  '<w:pgSz w:w="11906" w:h="16838"/>'
                  '<w:pgMar w:top="1701" w:right="1134" w:bottom="1134" w:left="1701" '
                  'w:header="1134" w:footer="1134" w:gutter="0"/>'
                  '</w:sectPr>')
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            '<w:document %s><w:body>%s%s</w:body></w:document>'
            % (NS, ''.join(_body), sect_final))


def main():
    build_body()
    parts = [
        ("[Content_Types].xml", CONTENT_TYPES),
        ("_rels/.rels", RELS),
        ("word/document.xml", document_xml()),
        ("word/_rels/document.xml.rels", DOC_RELS),
        ("word/styles.xml", STYLES),
        ("word/numbering.xml", NUMBERING),
        ("word/settings.xml", SETTINGS),
        ("word/header2.xml", header_xml()),
        ("docProps/core.xml", core_xml()),
        ("docProps/app.xml", APP_XML),
    ]
    with zipfile.ZipFile(DOCX_PATH, "w", zipfile.ZIP_DEFLATED) as z:
        for name, data in parts:
            z.writestr(name, data)
    doc = document_xml()
    print("Gerado: %s" % DOCX_PATH)
    print("Figuras: %d  | Quadros: %d  | Espacos de imagem a preencher: %d"
          % (_fig, _quad, doc.count("Inserir aqui a imagem")))
    print("Lembrete: abra no Word, selecione tudo (Ctrl+T) e tecle F9 para "
          "atualizar sumario, listas e paginacao.")


if __name__ == "__main__":
    main()
