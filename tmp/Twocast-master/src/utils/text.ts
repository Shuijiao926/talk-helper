/**
 * 计算合并后的字符长度，一个汉字算两个字符
 * @param text 文本
 * @returns 合并后的字符长度
 */
export function calcMergedChar(text: string) {
    let length = 0;
    for (let i = 0; i < text.length; i++) {
        const char = text.charAt(i);
        // 判断是否为汉字（Unicode范围：\u4e00-\u9fa5）
        if (char.match(/[\u4e00-\u9fa5]/)) {
            length += 2;
        } else {
            length += 1;
        }
    }
    return length;
}

/**
 * 计算文本的多个属性长度
 * @param text 文本
 * @returns 文本的多个属性长度
 */
export function multiTextLength(text: string) {
    let cntChar = 0, cntByte = 0, cntMergedChar = 0;
    cntChar += text.length
    cntByte += Buffer.byteLength(text, 'utf-8')
    cntMergedChar += calcMergedChar(text)
    return { cntChar, cntByte, cntMergedChar }
}