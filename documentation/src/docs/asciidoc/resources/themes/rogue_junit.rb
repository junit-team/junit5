require 'rouge' unless defined? ::Rouge.version

module Rouge; module Themes
  class Junit < CSSTheme
    name 'junit'

    style Comment,           fg: '#008800', italic: true
    style Error,             fg: '#a61717', bg: '#e3d2d2'
    style Str,               fg: '#0000ff'
    style Str::Char,         fg: '#800080'
    style Num,               fg: '#0000ff'
    style Keyword,           fg: '#000080', bold: true
    style Operator::Word,    bold: true
    style Name::Tag,         fg: '#0000ff', bold: true
    style Name::Attribute,   fg: '#ff0000'
    style Generic::Deleted,  fg: '#000000', bg: '#ffdddd', inline_block: true, extend: true
    style Generic::Inserted, fg: '#000000', bg: '#ddffdd', inline_block: true, extend: true
    style Text, {}



  end
end; end