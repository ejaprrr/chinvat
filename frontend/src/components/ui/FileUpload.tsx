import React from "react";

interface FileUploadProps {
  file: File | null;
  onChange: (file: File | null) => void;
  label: string;
  hint?: string;
  accept?: string;
  hintId?: string;
  fileNameEmpty?: string;
  actionLabel?: string;
  className?: string;
}

const FileUpload: React.FC<FileUploadProps> = ({
  file,
  onChange,
  label,
  hint,
  accept = ".p12,.pfx,.pem,.crt,.cer",
  hintId,
  fileNameEmpty = "No file selected",
  actionLabel = "Choose file",
  className = "",
}) => {
  return (
    <div className={className}>
      <label className="block text-sm font-medium mb-1">{label}</label>
      {hint && (
        <p id={hintId} className="text-xs text-muted mb-2">
          {hint}
        </p>
      )}
      <div className="flex items-center gap-3 border border-dashed rounded-xl px-4 py-3 bg-white">
        <span className="flex-1 truncate">
          {file ? (
            file.name
          ) : (
            <span className="text-muted">{fileNameEmpty}</span>
          )}
        </span>
        <label className="inline-flex items-center px-2 text-sm font-medium text-brand-500 underline cursor-pointer">
          {actionLabel}
          <input
            type="file"
            accept={accept}
            className="sr-only"
            onChange={(e) => onChange(e.target.files?.[0] ?? null)}
            aria-describedby={hintId}
          />
        </label>
      </div>
    </div>
  );
};

export default FileUpload;
