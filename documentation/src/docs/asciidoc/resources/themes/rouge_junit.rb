require 'rouge' unless defined? ::Rouge.version

module Rouge; module Themes
  class JunitDarkCode < CSSTheme
    name 'junit'

    # This is an extension of the official "Gruvbox" theme to remove accessibility issues in code blocks
    C_dark0_hard = '#1d2021'
    C_dark0 ='#282828'
    C_dark0_soft = '#32302f'
    C_dark1 = '#3c3836'
    C_dark2 = '#504945'
    C_dark3 = '#665c54'
    C_dark4 = '#7c6f64'
    C_dark4_256 = '#7c6f64'

    C_gray_245 = '#c2b9b2' # '#928374'
    C_gray_244 = '#c2b9b2' # '#928374'

    C_light0_hard = '#f9f5d7'
    C_light0 = '#fbf1c7'
    C_light0_soft = '#f2e5bc'
    C_light1 = '#ebdbb2'
    C_light2 = '#d5c4a1'
    C_light3 = '#bdae93'
    C_light4 = '#a89984'
    C_light4_256 = '#a89984'

    C_bright_red =  '#fca69c' # '#fb4934'
    C_bright_green = '#b8bb26'
    C_bright_yellow = '#fabd2f'
    C_bright_blue = '#83a598'
    C_bright_purple = '#d3869b'
    C_bright_aqua = '#8ec07c'
    C_bright_orange = '#fe8019'

    C_neutral_red = '#cc241d'
    C_neutral_green = '#98971a'
    C_neutral_yellow = '#d79921'
    C_neutral_blue = '#458588'
    C_neutral_purple = '#b16286'
    C_neutral_aqua = '#689d6a'
    C_neutral_orange = '#d65d0e'

    C_faded_red = '#9d0006'
    C_faded_green = '#79740e'
    C_faded_yellow = '#b57614'
    C_faded_blue = '#076678'
    C_faded_purple = '#8f3f71'
    C_faded_aqua = '#427b58'
    C_faded_orange = '#af3a03'


    extend HasModes

    def self.light!
    mode :dark # indicate that there is a dark variant
    mode! :light
    end

    def self.dark!
    mode :light # indicate that there is a light variant
    mode! :dark
    end

    def self.make_dark!
    palette bg0: C_dark0
    palette bg1: C_dark1
    palette bg2: C_dark2
    palette bg3: C_dark3
    palette bg4: C_dark4

    palette gray: C_gray_245

    palette fg0: C_light0
    palette fg1: C_light1
    palette fg2: C_light2
    palette fg3: C_light3
    palette fg4: C_light4

    palette fg4_256: C_light4_256

    palette red: C_bright_red
    palette green: C_bright_green
    palette yellow: C_bright_yellow
    palette blue: C_bright_blue
    palette purple: C_bright_purple
    palette aqua: C_bright_aqua
    palette orange: C_bright_orange

    end

    def self.make_light!
    palette bg0: C_light0
    palette bg1: C_light1
    palette bg2: C_light2
    palette bg3: C_light3
    palette bg4: C_light4

    palette gray: C_gray_244

    palette fg0: C_dark0
    palette fg1: C_dark1
    palette fg2: C_dark2
    palette fg3: C_dark3
    palette fg4: C_dark4

    palette fg4_256: C_dark4_256

    palette red: C_faded_red
    palette green: C_faded_green
    palette yellow: C_faded_yellow
    palette blue: C_faded_blue
    palette purple: C_faded_purple
    palette aqua: C_faded_aqua
    palette orange: C_faded_orange
    end

    dark!
    mode :light

    style Text, :fg => :fg0, :bg => :bg0
    style Error, :fg => :red, :bg => :bg0, :bold => true
    style Comment, :fg => :gray, :italic => true

    style Comment::Preproc, :fg => :aqua

    style Name::Tag, :fg => :red

    style Operator,
        Punctuation, :fg => :fg0

    style Generic::Inserted, :fg => :green, :bg => :bg0
    style Generic::Deleted, :fg => :red, :bg => :bg0
    style Generic::Heading, :fg => :green, :bold => true

    style Keyword, :fg => :red
    style Keyword::Constant, :fg => :purple
    style Keyword::Type, :fg => :yellow

    style Keyword::Declaration, :fg => :orange

    style Literal::String,
        Literal::String::Interpol,
        Literal::String::Regex, :fg => :green, :italic => true

    style Literal::String::Affix, :fg => :red

    style Literal::String::Escape, :fg => :orange

    style Name::Namespace,
        Name::Class, :fg => :aqua

    style Name::Constant, :fg => :purple

    style Name::Attribute, :fg => :green

    style Literal::Number, :fg => :purple

    style Literal::String::Symbol, :fg => :blue
  end

  class JunitLightCode < CSSTheme
    name 'junit_light'

     # This is an extension of the official "github" theme to remove accessibility issues in code blocks
     # Primer primitives (https://github.com/primer/primitives/tree/main/src/tokens)
    P_RED_0        = {:light => '#ffebe9', :dark => '#ffdcd7'}
    P_RED_3        = {:dark => '#ff7b72'}
    P_RED_5        = {:light => '#cf222e'}
    P_RED_7        = {:light => '#82071e', :dark => '#8e1519'}
    P_RED_8        = {:dark => '#67060c'}
    P_ORANGE_2     = {:dark => '#ffa657'}
    P_ORANGE_6     = {:light => '#953800'}
    P_GREEN_0      = {:light => '#dafbe1', :dark => '#aff5b4'}
    P_GREEN_1      = {:dark => '#7ee787'}
    P_GREEN_6      = {:light => '#116329'}
    P_GREEN_8      = {:dark => '#033a16'}
    P_BLUE_1       = {:dark => '#a5d6ff'}
    P_BLUE_2       = {:dark => '#79c0ff'}
    P_BLUE_5       = {:dark => '#1f6feb'}
    P_BLUE_6       = {:light => '#0550ae'}
    P_BLUE_8       = {:light => '#0a3069'}
    P_PURPLE_2     = {:dark => '#d2a8ff'}
    P_PURPLE_5     = {:light => '#8250df'}
    P_GRAY_0       = {:light => '#f6f8fa', :dark => '#f0f6fc'}
    P_GRAY_1       = {:dark => '#c9d1d9'}
    P_GRAY_3       = {:dark => '#8b949e'}
    P_GRAY_5       = {:light => '#34383d'} # '#6e7781'
    P_GRAY_8       = {:dark => '#161b22'}
    P_GRAY_9       = {:light => '#24292f'}

    extend HasModes

    def self.light!
     mode :dark # indicate that there is a dark variant
     mode! :light
    end

    def self.dark!
     mode :light # indicate that there is a light variant
     mode! :dark
    end

    def self.make_dark!
     palette :comment     => P_GRAY_3[@mode]
     palette :constant    => P_BLUE_2[@mode]
     palette :entity      => P_PURPLE_2[@mode]
     palette :heading     => P_BLUE_5[@mode]
     palette :keyword     => P_RED_3[@mode]
     palette :string      => P_BLUE_1[@mode]
     palette :tag         => P_GREEN_1[@mode]
     palette :variable    => P_ORANGE_2[@mode]

     palette :fgDefault   => P_GRAY_1[@mode]
     palette :bgDefault   => P_GRAY_8[@mode]

     palette :fgInserted  => P_GREEN_0[@mode]
     palette :bgInserted  => P_GREEN_8[@mode]

     palette :fgDeleted   => P_RED_0[@mode]
     palette :bgDeleted   => P_RED_8[@mode]

     palette :fgError     => P_GRAY_0[@mode]
     palette :bgError     => P_RED_7[@mode]
    end

    def self.make_light!
     palette :comment     => P_GRAY_5[@mode]
     palette :constant    => P_BLUE_6[@mode]
     palette :entity      => P_PURPLE_5[@mode]
     palette :heading     => P_BLUE_6[@mode]
     palette :keyword     => P_RED_5[@mode]
     palette :string      => P_BLUE_8[@mode]
     palette :tag         => P_GREEN_6[@mode]
     palette :variable    => P_ORANGE_6[@mode]

     palette :fgDefault   => P_GRAY_9[@mode]
     palette :bgDefault   => P_GRAY_0[@mode]

     palette :fgInserted  => P_GREEN_6[@mode]
     palette :bgInserted  => P_GREEN_0[@mode]

     palette :fgDeleted   => P_RED_7[@mode]
     palette :bgDeleted   => P_RED_0[@mode]

     palette :fgError     => P_GRAY_0[@mode]
     palette :bgError     => P_RED_7[@mode]
    end

    light!

    style Text,                       :fg => :fgDefault, :bg => :bgDefault

    style Keyword,                    :fg => :keyword

    style Generic::Error,             :fg => :fgError

    style Generic::Deleted,           :fg => :fgDeleted, :bg => :bgDeleted

    style Name::Builtin,
         Name::Class,
         Name::Constant,
         Name::Namespace,            :fg => :variable

    style Literal::String::Regex,
         Name::Attribute,
         Name::Tag,                  :fg => :tag

    style Generic::Inserted,          :fg => :fgInserted, :bg => :bgInserted

    style Keyword::Constant,
         Literal,
         Literal::String::Backtick,
         Name::Builtin::Pseudo,
         Name::Exception,
         Name::Label,
         Name::Property,
         Name::Variable,
         Operator,                   :fg => :constant

    style Generic::Heading,
         Generic::Subheading,        :fg => :heading, :bold => true

    style Literal::String,            :fg => :string

    style Name::Decorator,
         Name::Function,             :fg => :entity

    style Error,                      :fg => :fgError, :bg => :bgError

    style Comment,
         Generic::Lineno,
         Generic::Traceback,         :fg => :comment

    style Name::Entity,
         Literal::String::Interpol,  :fg => :fgDefault

    style Generic::Emph,              :fg => :fgDefault, :italic => true

    style Generic::Strong,            :fg => :fgDefault, :bold => true

  end
end; end

