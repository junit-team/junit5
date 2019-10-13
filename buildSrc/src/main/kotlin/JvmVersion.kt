enum class JvmVersion {

    `8` {
        override fun toString(): String = "1.$name"
    },

    `9`,
    `10`,
    `11`,
    `12`,
    `13`;

    override fun toString(): String = name
}
