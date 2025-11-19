package cn.ezandroid.lib.ezfilter.extra.sticker.model

import java.io.Serializable

/**
 * Sticker internal components
 *
 * @author like
 * @date 2018-01-05
 */
class Component : Serializable {
    // folder
    var src: String = ""

    // duration
    @JvmField
    var duration: Int = 0

    // Texture Anchor
    @JvmField
    var textureAnchor: TextureAnchor? = null

    // Original width of the material In order to adapt to multiple resolutions and save memory, the image loaded into memory may be smaller than the original material,
    // and the information such as anchor points is set according to the original size of the material, so it is recorded here
    @JvmField
    var width: Int = 0

    // Original height of the material In order to adapt to multiple resolutions and save memory, the image loaded into memory may be smaller than the original material,
    // and the information such as anchor points is set according to the original size of the material, so it is recorded here
    @JvmField
    var height: Int = 0
    var fileName: String = ""

    override fun toString(): String {
        return "Component{" +
                "src='" + src + '\'' +
                ", length=" + length +
                '}'
    }

    // Number of material files The number of valid files in the material folder
    @JvmField
    var length: Int = 0

    // Material file path list
    @JvmField
    var resources: List<String>? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
