pub struct Color256 {
    pub color_code: u8,
    pub hex_code: &'static str,
    pub rgb: [u8; 3],
}

impl Color256 {
    pub fn to_f64(&self) -> (f64, f64, f64) {
        (
            self.rgb[0] as f64 / 255.,
            self.rgb[1] as f64 / 255.,
            self.rgb[2] as f64 / 255.,
        )
    }

    pub fn to_f64_with_alpha(&self, alpha: f64) -> (f64, f64, f64, f64) {
        (
            self.rgb[0] as f64 / 255.,
            self.rgb[1] as f64 / 255.,
            self.rgb[2] as f64 / 255.,
            alpha
        )
    }

    pub fn get(color_256: i32) -> Color256 {
        match color_256 {
            0 => Color256 {
                color_code: 0,
                hex_code: "#000000",
                rgb: [0, 0, 0],
            },
            1 => Color256 {
                color_code: 1,
                hex_code: "#800000",
                rgb: [128, 0, 0],
            },
            2 => Color256 {
                color_code: 2,
                hex_code: "#008000",
                rgb: [0, 128, 0],
            },
            3 => Color256 {
                color_code: 3,
                hex_code: "#808000",
                rgb: [128, 128, 0],
            },
            4 => Color256 {
                color_code: 4,
                hex_code: "#000080",
                rgb: [0, 0, 128],
            },
            5 => Color256 {
                color_code: 5,
                hex_code: "#800080",
                rgb: [128, 0, 128],
            },
            6 => Color256 {
                color_code: 6,
                hex_code: "#008080",
                rgb: [0, 128, 128],
            },
            7 => Color256 {
                color_code: 7,
                hex_code: "#c0c0c0",
                rgb: [192, 192, 192],
            },
            8 => Color256 {
                color_code: 8,
                hex_code: "#808080",
                rgb: [128, 128, 128],
            },
            9 => Color256 {
                color_code: 9,
                hex_code: "#ff0000",
                rgb: [255, 0, 0],
            },
            10 => Color256 {
                color_code: 10,
                hex_code: "#00ff00",
                rgb: [0, 255, 0],
            },
            11 => Color256 {
                color_code: 11,
                hex_code: "#ffff00",
                rgb: [255, 255, 0],
            },
            12 => Color256 {
                color_code: 12,
                hex_code: "#0000ff",
                rgb: [0, 0, 255],
            },
            13 => Color256 {
                color_code: 13,
                hex_code: "#ff00ff",
                rgb: [255, 0, 255],
            },
            14 => Color256 {
                color_code: 14,
                hex_code: "#00ffff",
                rgb: [0, 255, 255],
            },
            15 => Color256 {
                color_code: 15,
                hex_code: "#ffffff",
                rgb: [255, 255, 255],
            },
            16 => Color256 {
                color_code: 16,
                hex_code: "#000000",
                rgb: [0, 0, 0],
            },
            17 => Color256 {
                color_code: 17,
                hex_code: "#00005f",
                rgb: [0, 0, 95],
            },
            18 => Color256 {
                color_code: 18,
                hex_code: "#000087",
                rgb: [0, 0, 135],
            },
            19 => Color256 {
                color_code: 19,
                hex_code: "#0000af",
                rgb: [0, 0, 175],
            },
            20 => Color256 {
                color_code: 20,
                hex_code: "#0000d7",
                rgb: [0, 0, 215],
            },
            21 => Color256 {
                color_code: 21,
                hex_code: "#0000ff",
                rgb: [0, 0, 255],
            },
            22 => Color256 {
                color_code: 22,
                hex_code: "#005f00",
                rgb: [0, 95, 0],
            },
            23 => Color256 {
                color_code: 23,
                hex_code: "#005f5f",
                rgb: [0, 95, 95],
            },
            24 => Color256 {
                color_code: 24,
                hex_code: "#005f87",
                rgb: [0, 95, 135],
            },
            25 => Color256 {
                color_code: 25,
                hex_code: "#005faf",
                rgb: [0, 95, 175],
            },
            26 => Color256 {
                color_code: 26,
                hex_code: "#005fd7",
                rgb: [0, 95, 215],
            },
            27 => Color256 {
                color_code: 27,
                hex_code: "#005fff",
                rgb: [0, 95, 255],
            },
            28 => Color256 {
                color_code: 28,
                hex_code: "#008700",
                rgb: [0, 135, 0],
            },
            29 => Color256 {
                color_code: 29,
                hex_code: "#00875f",
                rgb: [0, 135, 95],
            },
            30 => Color256 {
                color_code: 30,
                hex_code: "#008787",
                rgb: [0, 135, 135],
            },
            31 => Color256 {
                color_code: 31,
                hex_code: "#0087af",
                rgb: [0, 135, 175],
            },
            32 => Color256 {
                color_code: 32,
                hex_code: "#0087d7",
                rgb: [0, 135, 215],
            },
            33 => Color256 {
                color_code: 33,
                hex_code: "#0087ff",
                rgb: [0, 135, 255],
            },
            34 => Color256 {
                color_code: 34,
                hex_code: "#00af00",
                rgb: [0, 175, 0],
            },
            35 => Color256 {
                color_code: 35,
                hex_code: "#00af5f",
                rgb: [0, 175, 95],
            },
            36 => Color256 {
                color_code: 36,
                hex_code: "#00af87",
                rgb: [0, 175, 135],
            },
            37 => Color256 {
                color_code: 37,
                hex_code: "#00afaf",
                rgb: [0, 175, 175],
            },
            38 => Color256 {
                color_code: 38,
                hex_code: "#00afd7",
                rgb: [0, 175, 215],
            },
            39 => Color256 {
                color_code: 39,
                hex_code: "#00afff",
                rgb: [0, 175, 255],
            },
            40 => Color256 {
                color_code: 40,
                hex_code: "#00d700",
                rgb: [0, 215, 0],
            },
            41 => Color256 {
                color_code: 41,
                hex_code: "#00d75f",
                rgb: [0, 215, 95],
            },
            42 => Color256 {
                color_code: 42,
                hex_code: "#00d787",
                rgb: [0, 215, 135],
            },
            43 => Color256 {
                color_code: 43,
                hex_code: "#00d7af",
                rgb: [0, 215, 175],
            },
            44 => Color256 {
                color_code: 44,
                hex_code: "#00d7d7",
                rgb: [0, 215, 215],
            },
            45 => Color256 {
                color_code: 45,
                hex_code: "#00d7ff",
                rgb: [0, 215, 255],
            },
            46 => Color256 {
                color_code: 46,
                hex_code: "#00ff00",
                rgb: [0, 255, 0],
            },
            47 => Color256 {
                color_code: 47,
                hex_code: "#00ff5f",
                rgb: [0, 255, 95],
            },
            48 => Color256 {
                color_code: 48,
                hex_code: "#00ff87",
                rgb: [0, 255, 135],
            },
            49 => Color256 {
                color_code: 49,
                hex_code: "#00ffaf",
                rgb: [0, 255, 175],
            },
            50 => Color256 {
                color_code: 50,
                hex_code: "#00ffd7",
                rgb: [0, 255, 215],
            },
            51 => Color256 {
                color_code: 51,
                hex_code: "#00ffff",
                rgb: [0, 255, 255],
            },
            52 => Color256 {
                color_code: 52,
                hex_code: "#5f0000",
                rgb: [95, 0, 0],
            },
            53 => Color256 {
                color_code: 53,
                hex_code: "#5f005f",
                rgb: [95, 0, 95],
            },
            54 => Color256 {
                color_code: 54,
                hex_code: "#5f0087",
                rgb: [95, 0, 135],
            },
            55 => Color256 {
                color_code: 55,
                hex_code: "#5f00af",
                rgb: [95, 0, 175],
            },
            56 => Color256 {
                color_code: 56,
                hex_code: "#5f00d7",
                rgb: [95, 0, 215],
            },
            57 => Color256 {
                color_code: 57,
                hex_code: "#5f00ff",
                rgb: [95, 0, 255],
            },
            58 => Color256 {
                color_code: 58,
                hex_code: "#5f5f00",
                rgb: [95, 95, 0],
            },
            59 => Color256 {
                color_code: 59,
                hex_code: "#5f5f5f",
                rgb: [95, 95, 95],
            },
            60 => Color256 {
                color_code: 60,
                hex_code: "#5f5f87",
                rgb: [95, 95, 135],
            },
            61 => Color256 {
                color_code: 61,
                hex_code: "#5f5faf",
                rgb: [95, 95, 175],
            },
            62 => Color256 {
                color_code: 62,
                hex_code: "#5f5fd7",
                rgb: [95, 95, 215],
            },
            63 => Color256 {
                color_code: 63,
                hex_code: "#5f5fff",
                rgb: [95, 95, 255],
            },
            64 => Color256 {
                color_code: 64,
                hex_code: "#5f8700",
                rgb: [95, 135, 0],
            },
            65 => Color256 {
                color_code: 65,
                hex_code: "#5f875f",
                rgb: [95, 135, 95],
            },
            66 => Color256 {
                color_code: 66,
                hex_code: "#5f8787",
                rgb: [95, 135, 135],
            },
            67 => Color256 {
                color_code: 67,
                hex_code: "#5f87af",
                rgb: [95, 135, 175],
            },
            68 => Color256 {
                color_code: 68,
                hex_code: "#5f87d7",
                rgb: [95, 135, 215],
            },
            69 => Color256 {
                color_code: 69,
                hex_code: "#5f87ff",
                rgb: [95, 135, 255],
            },
            70 => Color256 {
                color_code: 70,
                hex_code: "#5faf00",
                rgb: [95, 175, 0],
            },
            71 => Color256 {
                color_code: 71,
                hex_code: "#5faf5f",
                rgb: [95, 175, 95],
            },
            72 => Color256 {
                color_code: 72,
                hex_code: "#5faf87",
                rgb: [95, 175, 135],
            },
            73 => Color256 {
                color_code: 73,
                hex_code: "#5fafaf",
                rgb: [95, 175, 175],
            },
            74 => Color256 {
                color_code: 74,
                hex_code: "#5fafd7",
                rgb: [95, 175, 215],
            },
            75 => Color256 {
                color_code: 75,
                hex_code: "#5fafff",
                rgb: [95, 175, 255],
            },
            76 => Color256 {
                color_code: 76,
                hex_code: "#5fd700",
                rgb: [95, 215, 0],
            },
            77 => Color256 {
                color_code: 77,
                hex_code: "#5fd75f",
                rgb: [95, 215, 95],
            },
            78 => Color256 {
                color_code: 78,
                hex_code: "#5fd787",
                rgb: [95, 215, 135],
            },
            79 => Color256 {
                color_code: 79,
                hex_code: "#5fd7af",
                rgb: [95, 215, 175],
            },
            80 => Color256 {
                color_code: 80,
                hex_code: "#5fd7d7",
                rgb: [95, 215, 215],
            },
            81 => Color256 {
                color_code: 81,
                hex_code: "#5fd7ff",
                rgb: [95, 215, 255],
            },
            82 => Color256 {
                color_code: 82,
                hex_code: "#5fff00",
                rgb: [95, 255, 0],
            },
            83 => Color256 {
                color_code: 83,
                hex_code: "#5fff5f",
                rgb: [95, 255, 95],
            },
            84 => Color256 {
                color_code: 84,
                hex_code: "#5fff87",
                rgb: [95, 255, 135],
            },
            85 => Color256 {
                color_code: 85,
                hex_code: "#5fffaf",
                rgb: [95, 255, 175],
            },
            86 => Color256 {
                color_code: 86,
                hex_code: "#5fffd7",
                rgb: [95, 255, 215],
            },
            87 => Color256 {
                color_code: 87,
                hex_code: "#5fffff",
                rgb: [95, 255, 255],
            },
            88 => Color256 {
                color_code: 88,
                hex_code: "#870000",
                rgb: [135, 0, 0],
            },
            89 => Color256 {
                color_code: 89,
                hex_code: "#87005f",
                rgb: [135, 0, 95],
            },
            90 => Color256 {
                color_code: 90,
                hex_code: "#870087",
                rgb: [135, 0, 135],
            },
            91 => Color256 {
                color_code: 91,
                hex_code: "#8700af",
                rgb: [135, 0, 175],
            },
            92 => Color256 {
                color_code: 92,
                hex_code: "#8700d7",
                rgb: [135, 0, 215],
            },
            93 => Color256 {
                color_code: 93,
                hex_code: "#8700ff",
                rgb: [135, 0, 255],
            },
            94 => Color256 {
                color_code: 94,
                hex_code: "#875f00",
                rgb: [135, 95, 0],
            },
            95 => Color256 {
                color_code: 95,
                hex_code: "#875f5f",
                rgb: [135, 95, 95],
            },
            96 => Color256 {
                color_code: 96,
                hex_code: "#875f87",
                rgb: [135, 95, 135],
            },
            97 => Color256 {
                color_code: 97,
                hex_code: "#875faf",
                rgb: [135, 95, 175],
            },
            98 => Color256 {
                color_code: 98,
                hex_code: "#875fd7",
                rgb: [135, 95, 215],
            },
            99 => Color256 {
                color_code: 99,
                hex_code: "#875fff",
                rgb: [135, 95, 255],
            },
            100 => Color256 {
                color_code: 100,
                hex_code: "#878700",
                rgb: [135, 135, 0],
            },
            101 => Color256 {
                color_code: 101,
                hex_code: "#87875f",
                rgb: [135, 135, 95],
            },
            102 => Color256 {
                color_code: 102,
                hex_code: "#878787",
                rgb: [135, 135, 135],
            },
            103 => Color256 {
                color_code: 103,
                hex_code: "#8787af",
                rgb: [135, 135, 175],
            },
            104 => Color256 {
                color_code: 104,
                hex_code: "#8787d7",
                rgb: [135, 135, 215],
            },
            105 => Color256 {
                color_code: 105,
                hex_code: "#8787ff",
                rgb: [135, 135, 255],
            },
            106 => Color256 {
                color_code: 106,
                hex_code: "#87af00",
                rgb: [135, 175, 0],
            },
            107 => Color256 {
                color_code: 107,
                hex_code: "#87af5f",
                rgb: [135, 175, 95],
            },
            108 => Color256 {
                color_code: 108,
                hex_code: "#87af87",
                rgb: [135, 175, 135],
            },
            109 => Color256 {
                color_code: 109,
                hex_code: "#87afaf",
                rgb: [135, 175, 175],
            },
            110 => Color256 {
                color_code: 110,
                hex_code: "#87afd7",
                rgb: [135, 175, 215],
            },
            111 => Color256 {
                color_code: 111,
                hex_code: "#87afff",
                rgb: [135, 175, 255],
            },
            112 => Color256 {
                color_code: 112,
                hex_code: "#87d700",
                rgb: [135, 215, 0],
            },
            113 => Color256 {
                color_code: 113,
                hex_code: "#87d75f",
                rgb: [135, 215, 95],
            },
            114 => Color256 {
                color_code: 114,
                hex_code: "#87d787",
                rgb: [135, 215, 135],
            },
            115 => Color256 {
                color_code: 115,
                hex_code: "#87d7af",
                rgb: [135, 215, 175],
            },
            116 => Color256 {
                color_code: 116,
                hex_code: "#87d7d7",
                rgb: [135, 215, 215],
            },
            117 => Color256 {
                color_code: 117,
                hex_code: "#87d7ff",
                rgb: [135, 215, 255],
            },
            118 => Color256 {
                color_code: 118,
                hex_code: "#87ff00",
                rgb: [135, 255, 0],
            },
            119 => Color256 {
                color_code: 119,
                hex_code: "#87ff5f",
                rgb: [135, 255, 95],
            },
            120 => Color256 {
                color_code: 120,
                hex_code: "#87ff87",
                rgb: [135, 255, 135],
            },
            121 => Color256 {
                color_code: 121,
                hex_code: "#87ffaf",
                rgb: [135, 255, 175],
            },
            122 => Color256 {
                color_code: 122,
                hex_code: "#87ffd7",
                rgb: [135, 255, 215],
            },
            123 => Color256 {
                color_code: 123,
                hex_code: "#87ffff",
                rgb: [135, 255, 255],
            },
            124 => Color256 {
                color_code: 124,
                hex_code: "#af0000",
                rgb: [175, 0, 0],
            },
            125 => Color256 {
                color_code: 125,
                hex_code: "#af005f",
                rgb: [175, 0, 95],
            },
            126 => Color256 {
                color_code: 126,
                hex_code: "#af0087",
                rgb: [175, 0, 135],
            },
            127 => Color256 {
                color_code: 127,
                hex_code: "#af00af",
                rgb: [175, 0, 175],
            },
            128 => Color256 {
                color_code: 128,
                hex_code: "#af00d7",
                rgb: [175, 0, 215],
            },
            129 => Color256 {
                color_code: 129,
                hex_code: "#af00ff",
                rgb: [175, 0, 255],
            },
            130 => Color256 {
                color_code: 130,
                hex_code: "#af5f00",
                rgb: [175, 95, 0],
            },
            131 => Color256 {
                color_code: 131,
                hex_code: "#af5f5f",
                rgb: [175, 95, 95],
            },
            132 => Color256 {
                color_code: 132,
                hex_code: "#af5f87",
                rgb: [175, 95, 135],
            },
            133 => Color256 {
                color_code: 133,
                hex_code: "#af5faf",
                rgb: [175, 95, 175],
            },
            134 => Color256 {
                color_code: 134,
                hex_code: "#af5fd7",
                rgb: [175, 95, 215],
            },
            135 => Color256 {
                color_code: 135,
                hex_code: "#af5fff",
                rgb: [175, 95, 255],
            },
            136 => Color256 {
                color_code: 136,
                hex_code: "#af8700",
                rgb: [175, 135, 0],
            },
            137 => Color256 {
                color_code: 137,
                hex_code: "#af875f",
                rgb: [175, 135, 95],
            },
            138 => Color256 {
                color_code: 138,
                hex_code: "#af8787",
                rgb: [175, 135, 135],
            },
            139 => Color256 {
                color_code: 139,
                hex_code: "#af87af",
                rgb: [175, 135, 175],
            },
            140 => Color256 {
                color_code: 140,
                hex_code: "#af87d7",
                rgb: [175, 135, 215],
            },
            141 => Color256 {
                color_code: 141,
                hex_code: "#af87ff",
                rgb: [175, 135, 255],
            },
            142 => Color256 {
                color_code: 142,
                hex_code: "#afaf00",
                rgb: [175, 175, 0],
            },
            143 => Color256 {
                color_code: 143,
                hex_code: "#afaf5f",
                rgb: [175, 175, 95],
            },
            144 => Color256 {
                color_code: 144,
                hex_code: "#afaf87",
                rgb: [175, 175, 135],
            },
            145 => Color256 {
                color_code: 145,
                hex_code: "#afafaf",
                rgb: [175, 175, 175],
            },
            146 => Color256 {
                color_code: 146,
                hex_code: "#afafd7",
                rgb: [175, 175, 215],
            },
            147 => Color256 {
                color_code: 147,
                hex_code: "#afafff",
                rgb: [175, 175, 255],
            },
            148 => Color256 {
                color_code: 148,
                hex_code: "#afd700",
                rgb: [175, 215, 0],
            },
            149 => Color256 {
                color_code: 149,
                hex_code: "#afd75f",
                rgb: [175, 215, 95],
            },
            150 => Color256 {
                color_code: 150,
                hex_code: "#afd787",
                rgb: [175, 215, 135],
            },
            151 => Color256 {
                color_code: 151,
                hex_code: "#afd7af",
                rgb: [175, 215, 175],
            },
            152 => Color256 {
                color_code: 152,
                hex_code: "#afd7d7",
                rgb: [175, 215, 215],
            },
            153 => Color256 {
                color_code: 153,
                hex_code: "#afd7ff",
                rgb: [175, 215, 255],
            },
            154 => Color256 {
                color_code: 154,
                hex_code: "#afff00",
                rgb: [175, 255, 0],
            },
            155 => Color256 {
                color_code: 155,
                hex_code: "#afff5f",
                rgb: [175, 255, 95],
            },
            156 => Color256 {
                color_code: 156,
                hex_code: "#afff87",
                rgb: [175, 255, 135],
            },
            157 => Color256 {
                color_code: 157,
                hex_code: "#afffaf",
                rgb: [175, 255, 175],
            },
            158 => Color256 {
                color_code: 158,
                hex_code: "#afffd7",
                rgb: [175, 255, 215],
            },
            159 => Color256 {
                color_code: 159,
                hex_code: "#afffff",
                rgb: [175, 255, 255],
            },
            160 => Color256 {
                color_code: 160,
                hex_code: "#d70000",
                rgb: [215, 0, 0],
            },
            161 => Color256 {
                color_code: 161,
                hex_code: "#d7005f",
                rgb: [215, 0, 95],
            },
            162 => Color256 {
                color_code: 162,
                hex_code: "#d70087",
                rgb: [215, 0, 135],
            },
            163 => Color256 {
                color_code: 163,
                hex_code: "#d700af",
                rgb: [215, 0, 175],
            },
            164 => Color256 {
                color_code: 164,
                hex_code: "#d700d7",
                rgb: [215, 0, 215],
            },
            165 => Color256 {
                color_code: 165,
                hex_code: "#d700ff",
                rgb: [215, 0, 255],
            },
            166 => Color256 {
                color_code: 166,
                hex_code: "#d75f00",
                rgb: [215, 95, 0],
            },
            167 => Color256 {
                color_code: 167,
                hex_code: "#d75f5f",
                rgb: [215, 95, 95],
            },
            168 => Color256 {
                color_code: 168,
                hex_code: "#d75f87",
                rgb: [215, 95, 135],
            },
            169 => Color256 {
                color_code: 169,
                hex_code: "#d75faf",
                rgb: [215, 95, 175],
            },
            170 => Color256 {
                color_code: 170,
                hex_code: "#d75fd7",
                rgb: [215, 95, 215],
            },
            171 => Color256 {
                color_code: 171,
                hex_code: "#d75fff",
                rgb: [215, 95, 255],
            },
            172 => Color256 {
                color_code: 172,
                hex_code: "#d78700",
                rgb: [215, 135, 0],
            },
            173 => Color256 {
                color_code: 173,
                hex_code: "#d7875f",
                rgb: [215, 135, 95],
            },
            174 => Color256 {
                color_code: 174,
                hex_code: "#d78787",
                rgb: [215, 135, 135],
            },
            175 => Color256 {
                color_code: 175,
                hex_code: "#d787af",
                rgb: [215, 135, 175],
            },
            176 => Color256 {
                color_code: 176,
                hex_code: "#d787d7",
                rgb: [215, 135, 215],
            },
            177 => Color256 {
                color_code: 177,
                hex_code: "#d787ff",
                rgb: [215, 135, 255],
            },
            178 => Color256 {
                color_code: 178,
                hex_code: "#d7af00",
                rgb: [215, 175, 0],
            },
            179 => Color256 {
                color_code: 179,
                hex_code: "#d7af5f",
                rgb: [215, 175, 95],
            },
            180 => Color256 {
                color_code: 180,
                hex_code: "#d7af87",
                rgb: [215, 175, 135],
            },
            181 => Color256 {
                color_code: 181,
                hex_code: "#d7afaf",
                rgb: [215, 175, 175],
            },
            182 => Color256 {
                color_code: 182,
                hex_code: "#d7afd7",
                rgb: [215, 175, 215],
            },
            183 => Color256 {
                color_code: 183,
                hex_code: "#d7afff",
                rgb: [215, 175, 255],
            },
            184 => Color256 {
                color_code: 184,
                hex_code: "#d7d700",
                rgb: [215, 215, 0],
            },
            185 => Color256 {
                color_code: 185,
                hex_code: "#d7d75f",
                rgb: [215, 215, 95],
            },
            186 => Color256 {
                color_code: 186,
                hex_code: "#d7d787",
                rgb: [215, 215, 135],
            },
            187 => Color256 {
                color_code: 187,
                hex_code: "#d7d7af",
                rgb: [215, 215, 175],
            },
            188 => Color256 {
                color_code: 188,
                hex_code: "#d7d7d7",
                rgb: [215, 215, 215],
            },
            189 => Color256 {
                color_code: 189,
                hex_code: "#d7d7ff",
                rgb: [215, 215, 255],
            },
            190 => Color256 {
                color_code: 190,
                hex_code: "#d7ff00",
                rgb: [215, 255, 0],
            },
            191 => Color256 {
                color_code: 191,
                hex_code: "#d7ff5f",
                rgb: [215, 255, 95],
            },
            192 => Color256 {
                color_code: 192,
                hex_code: "#d7ff87",
                rgb: [215, 255, 135],
            },
            193 => Color256 {
                color_code: 193,
                hex_code: "#d7ffaf",
                rgb: [215, 255, 175],
            },
            194 => Color256 {
                color_code: 194,
                hex_code: "#d7ffd7",
                rgb: [215, 255, 215],
            },
            195 => Color256 {
                color_code: 195,
                hex_code: "#d7ffff",
                rgb: [215, 255, 255],
            },
            196 => Color256 {
                color_code: 196,
                hex_code: "#ff0000",
                rgb: [255, 0, 0],
            },
            197 => Color256 {
                color_code: 197,
                hex_code: "#ff005f",
                rgb: [255, 0, 95],
            },
            198 => Color256 {
                color_code: 198,
                hex_code: "#ff0087",
                rgb: [255, 0, 135],
            },
            199 => Color256 {
                color_code: 199,
                hex_code: "#ff00af",
                rgb: [255, 0, 175],
            },
            200 => Color256 {
                color_code: 200,
                hex_code: "#ff00d7",
                rgb: [255, 0, 215],
            },
            201 => Color256 {
                color_code: 201,
                hex_code: "#ff00ff",
                rgb: [255, 0, 255],
            },
            202 => Color256 {
                color_code: 202,
                hex_code: "#ff5f00",
                rgb: [255, 95, 0],
            },
            203 => Color256 {
                color_code: 203,
                hex_code: "#ff5f5f",
                rgb: [255, 95, 95],
            },
            204 => Color256 {
                color_code: 204,
                hex_code: "#ff5f87",
                rgb: [255, 95, 135],
            },
            205 => Color256 {
                color_code: 205,
                hex_code: "#ff5faf",
                rgb: [255, 95, 175],
            },
            206 => Color256 {
                color_code: 206,
                hex_code: "#ff5fd7",
                rgb: [255, 95, 215],
            },
            207 => Color256 {
                color_code: 207,
                hex_code: "#ff5fff",
                rgb: [255, 95, 255],
            },
            208 => Color256 {
                color_code: 208,
                hex_code: "#ff8700",
                rgb: [255, 135, 0],
            },
            209 => Color256 {
                color_code: 209,
                hex_code: "#ff875f",
                rgb: [255, 135, 95],
            },
            210 => Color256 {
                color_code: 210,
                hex_code: "#ff8787",
                rgb: [255, 135, 135],
            },
            211 => Color256 {
                color_code: 211,
                hex_code: "#ff87af",
                rgb: [255, 135, 175],
            },
            212 => Color256 {
                color_code: 212,
                hex_code: "#ff87d7",
                rgb: [255, 135, 215],
            },
            213 => Color256 {
                color_code: 213,
                hex_code: "#ff87ff",
                rgb: [255, 135, 255],
            },
            214 => Color256 {
                color_code: 214,
                hex_code: "#ffaf00",
                rgb: [255, 175, 0],
            },
            215 => Color256 {
                color_code: 215,
                hex_code: "#ffaf5f",
                rgb: [255, 175, 95],
            },
            216 => Color256 {
                color_code: 216,
                hex_code: "#ffaf87",
                rgb: [255, 175, 135],
            },
            217 => Color256 {
                color_code: 217,
                hex_code: "#ffafaf",
                rgb: [255, 175, 175],
            },
            218 => Color256 {
                color_code: 218,
                hex_code: "#ffafd7",
                rgb: [255, 175, 215],
            },
            219 => Color256 {
                color_code: 219,
                hex_code: "#ffafff",
                rgb: [255, 175, 255],
            },
            220 => Color256 {
                color_code: 220,
                hex_code: "#ffd700",
                rgb: [255, 215, 0],
            },
            221 => Color256 {
                color_code: 221,
                hex_code: "#ffd75f",
                rgb: [255, 215, 95],
            },
            222 => Color256 {
                color_code: 222,
                hex_code: "#ffd787",
                rgb: [255, 215, 135],
            },
            223 => Color256 {
                color_code: 223,
                hex_code: "#ffd7af",
                rgb: [255, 215, 175],
            },
            224 => Color256 {
                color_code: 224,
                hex_code: "#ffd7d7",
                rgb: [255, 215, 215],
            },
            225 => Color256 {
                color_code: 225,
                hex_code: "#ffd7ff",
                rgb: [255, 215, 255],
            },
            226 => Color256 {
                color_code: 226,
                hex_code: "#ffff00",
                rgb: [255, 255, 0],
            },
            227 => Color256 {
                color_code: 227,
                hex_code: "#ffff5f",
                rgb: [255, 255, 95],
            },
            228 => Color256 {
                color_code: 228,
                hex_code: "#ffff87",
                rgb: [255, 255, 135],
            },
            229 => Color256 {
                color_code: 229,
                hex_code: "#ffffaf",
                rgb: [255, 255, 175],
            },
            230 => Color256 {
                color_code: 230,
                hex_code: "#ffffd7",
                rgb: [255, 255, 215],
            },
            231 => Color256 {
                color_code: 231,
                hex_code: "#ffffff",
                rgb: [255, 255, 255],
            },
            232 => Color256 {
                color_code: 232,
                hex_code: "#080808",
                rgb: [8, 8, 8],
            },
            233 => Color256 {
                color_code: 233,
                hex_code: "#121212",
                rgb: [18, 18, 18],
            },
            234 => Color256 {
                color_code: 234,
                hex_code: "#1c1c1c",
                rgb: [28, 28, 28],
            },
            235 => Color256 {
                color_code: 235,
                hex_code: "#262626",
                rgb: [38, 38, 38],
            },
            236 => Color256 {
                color_code: 236,
                hex_code: "#303030",
                rgb: [48, 48, 48],
            },
            237 => Color256 {
                color_code: 237,
                hex_code: "#3a3a3a",
                rgb: [58, 58, 58],
            },
            238 => Color256 {
                color_code: 238,
                hex_code: "#444444",
                rgb: [68, 68, 68],
            },
            239 => Color256 {
                color_code: 239,
                hex_code: "#4e4e4e",
                rgb: [78, 78, 78],
            },
            240 => Color256 {
                color_code: 240,
                hex_code: "#585858",
                rgb: [88, 88, 88],
            },
            241 => Color256 {
                color_code: 241,
                hex_code: "#626262",
                rgb: [98, 98, 98],
            },
            242 => Color256 {
                color_code: 242,
                hex_code: "#6c6c6c",
                rgb: [108, 108, 108],
            },
            243 => Color256 {
                color_code: 243,
                hex_code: "#767676",
                rgb: [118, 118, 118],
            },
            244 => Color256 {
                color_code: 244,
                hex_code: "#808080",
                rgb: [128, 128, 128],
            },
            245 => Color256 {
                color_code: 245,
                hex_code: "#8a8a8a",
                rgb: [138, 138, 138],
            },
            246 => Color256 {
                color_code: 246,
                hex_code: "#949494",
                rgb: [148, 148, 148],
            },
            247 => Color256 {
                color_code: 247,
                hex_code: "#9e9e9e",
                rgb: [158, 158, 158],
            },
            248 => Color256 {
                color_code: 248,
                hex_code: "#a8a8a8",
                rgb: [168, 168, 168],
            },
            249 => Color256 {
                color_code: 249,
                hex_code: "#b2b2b2",
                rgb: [178, 178, 178],
            },
            250 => Color256 {
                color_code: 250,
                hex_code: "#bcbcbc",
                rgb: [188, 188, 188],
            },
            251 => Color256 {
                color_code: 251,
                hex_code: "#c6c6c6",
                rgb: [198, 198, 198],
            },
            252 => Color256 {
                color_code: 252,
                hex_code: "#d0d0d0",
                rgb: [208, 208, 208],
            },
            253 => Color256 {
                color_code: 253,
                hex_code: "#dadada",
                rgb: [218, 218, 218],
            },
            254 => Color256 {
                color_code: 254,
                hex_code: "#e4e4e4",
                rgb: [228, 228, 228],
            },
            255 => Color256 {
                color_code: 255,
                hex_code: "#eeeeee",
                rgb: [238, 238, 238],
            },
            _ => unimplemented!(),
        }
    }
}
