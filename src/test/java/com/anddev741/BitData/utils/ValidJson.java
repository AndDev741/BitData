package com.anddev741.BitData.utils;

public class ValidJson {
    public static String getValidJson() {
        return """
                        {
                "op": "utx",
                "x": {
                    "lock_time": 0,
                    "ver": 1,
                    "size": 196,
                    "inputs": [
                        {
                            "sequence": 4294967295,
                            "prev_out": {
                                "spent": true,
                                "tx_index": 0,
                                "type": 0,
                                "addr": "bc1pwrctrmp9fn3vprvlm4spwnskrfx3dpwk80hsh9849fhehperfkuqsz8n4m",
                                "value": 6371870,
                                "n": 0,
                                "script": "512070f0b1ec254ce2c08d9fdd60174e161a4d1685d63bef0b94f52a6f9b87234db8"
                            },
                            "script": ""
                        }
                    ],
                    "time": 1761907754,
                    "tx_index": 0,
                    "vin_sz": 1,
                    "hash": "281640e906277cff4ee91167c54d940b05d53fc851291b6e6afcaaa538eaabce",
                    "vout_sz": 2,
                    "relayed_by": "0.0.0.0",
                    "out": [
                        {
                            "spent": false,
                            "tx_index": 0,
                            "type": 0,
                            "addr": "17y4HnqePx8moCxJqRsRp8tQmNQwzuqUFp",
                            "value": 101907,
                            "n": 0,
                            "script": "76a9144c6acb63a8c28824b0ac332240153fa927ad61a088ac"
                        },
                        {
                            "spent": false,
                            "tx_index": 0,
                            "type": 0,
                            "addr": "bc1pywnfngfvzu8kjg5jc5tmcgmwfm6l7377kj38c8aet7dc383jmxyqx0rgfs",
                            "value": 6269379,
                            "n": 1,
                            "script": "512023a699a12c170f692292c517bc236e4ef5ff47deb4a27c1fb95f9b889e32d988"
                        }
                    ]
                }
            }
            """;
    }
}
