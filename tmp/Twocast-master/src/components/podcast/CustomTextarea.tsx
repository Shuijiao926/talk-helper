// 共用的 Textarea 组件
interface CustomTextareaProps {
    value: string;
    onChange: (value: string) => void;
    placeholder: string;
    rows?: number;
    disabled?: boolean;
}

export function CustomTextarea({ value, onChange, placeholder, rows = 3, disabled = false }: CustomTextareaProps) {
    return (
        <div className="relative">
            <textarea
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                rows={rows}
                className="w-full px-3 sm:px-4 py-2 sm:py-3 text-sm sm:text-base bg-gradient-to-br from-white/90 to-white/60 dark:from-gray-800/90 dark:to-gray-900/60 backdrop-blur-sm border-0 rounded-lg sm:rounded-xl focus:outline-none focus:ring-0 text-gray-800 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 resize-y min-h-[80px] shadow-inner"
                style={{
                    boxShadow: 'inset 0 2px 10px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.3)'
                }}
                disabled={disabled}
            />
            {/* 光泽效果覆盖层 */}
            <div className="absolute inset-0 rounded-lg sm:rounded-xl bg-gradient-to-br from-white/20 to-transparent pointer-events-none"></div>
        </div>
    );
}